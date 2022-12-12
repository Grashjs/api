package com.grash.controller;

import com.grash.dto.ReadingPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.*;
import com.grash.model.enums.NotificationType;
import com.grash.model.enums.WorkOrderMeterTriggerCondition;
import com.grash.service.*;
import com.grash.utils.ReadingComparator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/readings")
@Api(tags = "reading")
@RequiredArgsConstructor
public class ReadingController {

    private final MeterService meterService;
    private final ReadingService readingService;
    private final UserService userService;
    private final WorkOrderMeterTriggerService workOrderMeterTriggerService;
    private final NotificationService notificationService;
    private final WorkOrderService workOrderService;


    @GetMapping("/meter/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Reading not found")})
    public Collection<Reading> getByMeter(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Meter> optionalMeter = meterService.findById(id);
        if (optionalMeter.isPresent() && meterService.hasAccess(user, optionalMeter.get())) {
            return readingService.findByMeter(id);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public Reading create(@ApiParam("Reading") @Valid @RequestBody Reading readingReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (readingService.canCreate(user, readingReq)) {
            Optional<Meter> optionalMeter = meterService.findById(readingReq.getMeter().getId());
            if (optionalMeter.isPresent()) {
                Meter meter = optionalMeter.get();
                Collection<Reading> readings = readingService.findByMeter(readingReq.getMeter().getId());
                if (!readings.isEmpty()) {
                    Reading lastReading = Collections.min(readings, new ReadingComparator());
                    long daysBetweenLastAndToday = ChronoUnit.DAYS.between(lastReading.getCreatedAt(), new Date().toInstant());
                    if (daysBetweenLastAndToday < meter.getUpdateFrequency()) {
                        throw new CustomException("The update frequency has not been respected", HttpStatus.NOT_ACCEPTABLE);
                    }
                }
                Collection<WorkOrderMeterTrigger> meterTriggers = workOrderMeterTriggerService.findByMeter(meter.getId());
                meterTriggers.forEach(meterTrigger -> {
                    boolean error = false;
                    StringBuilder message = new StringBuilder();
                    if (meterTrigger.getTriggerCondition().equals(WorkOrderMeterTriggerCondition.LESS_THAN)) {
                        if (readingReq.getValue() < meterTrigger.getValue()) {
                            error = true;
                            message.append(meter.getName()).append(" value is less than ").append(meterTrigger.getValue()).append(meter.getUnit());
                        }
                    } else if (readingReq.getValue() > meterTrigger.getValue()) {
                        error = true;
                        message.append(meter.getName()).append(" value is more than ").append(meterTrigger.getValue()).append(meter.getUnit());
                    }
                    if (error) {
                        meter.getUsers().forEach(user1 -> {
                            notificationService.create(new Notification(message.toString(), user1, NotificationType.METER, meter.getId()));
                        });
                        WorkOrder workOrder = workOrderService.getWorkOrderFromWorkOrderBase(meterTrigger);
                        WorkOrder createdWorkOrder = workOrderService.create(workOrder);
                        workOrderService.notify(createdWorkOrder);
                    }
                });
                return readingService.create(readingReq);
            } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Reading not found")})
    public Reading patch(@ApiParam("Reading") @Valid @RequestBody ReadingPatchDTO reading, @ApiParam("id") @PathVariable("id") Long id,
                         HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Reading> optionalReading = readingService.findById(id);

        if (optionalReading.isPresent()) {
            Reading savedReading = optionalReading.get();
            if (readingService.hasAccess(user, savedReading) && readingService.canPatch(user, reading)) {
                return readingService.update(id, reading);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Reading not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Reading not found")})
    public ResponseEntity<SuccessResponse> delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Reading> optionalReading = readingService.findById(id);
        if (optionalReading.isPresent()) {
            Reading savedReading = optionalReading.get();
            if (readingService.hasAccess(user, savedReading)) {
                readingService.delete(id);
                return new ResponseEntity<>(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Reading not found", HttpStatus.NOT_FOUND);
    }
}
