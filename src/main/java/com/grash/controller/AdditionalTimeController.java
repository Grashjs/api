package com.grash.controller;

import com.grash.dto.AdditionalTimePatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.AdditionalTime;
import com.grash.model.OwnUser;
import com.grash.model.WorkOrder;
import com.grash.model.enums.PlanFeatures;
import com.grash.model.enums.Status;
import com.grash.model.enums.TimeStatus;
import com.grash.service.AdditionalTimeService;
import com.grash.service.UserService;
import com.grash.service.WorkOrderService;
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
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/additional-times")
@Api(tags = "additionalTime")
@RequiredArgsConstructor
public class AdditionalTimeController {

    private final AdditionalTimeService additionalTimeService;
    private final UserService userService;
    private final WorkOrderService workOrderService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AdditionalTime not found")})
    public AdditionalTime getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<AdditionalTime> optionalAdditionalTime = additionalTimeService.findById(id);
        if (optionalAdditionalTime.isPresent()) {
            AdditionalTime savedAdditionalTime = optionalAdditionalTime.get();
            if (additionalTimeService.hasAccess(user, savedAdditionalTime)) {
                return savedAdditionalTime;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);

    }

    @GetMapping("/work-order/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AdditionalTime not found")})
    public Collection<AdditionalTime> getByWorkOrder(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(id);
        if (optionalWorkOrder.isPresent() && workOrderService.hasAccess(user, optionalWorkOrder.get())) {
            return additionalTimeService.findByWorkOrder(id);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/work-order/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AdditionalTime not found")})
    public AdditionalTime controlTimer(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req, @RequestParam(defaultValue = "true") boolean start) {
        OwnUser user = userService.whoami(req);
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(id);
        if (optionalWorkOrder.isPresent() && workOrderService.hasAccess(user, optionalWorkOrder.get())) {
            Optional<AdditionalTime> optionalAdditionalTime = additionalTimeService.findByWorkOrder(id).stream().filter(additionalTime -> additionalTime.isPrimaryTime() && additionalTime.getAssignedTo().getId().equals(user.getId())).findFirst();
            if (start) {
                WorkOrder workOrder = optionalWorkOrder.get();
                if (!workOrder.getStatus().equals(Status.IN_PROGRESS)) {
                    workOrder.setStatus(Status.IN_PROGRESS);
                    workOrderService.save(workOrder);
                }
                if (optionalAdditionalTime.isPresent()) {
                    AdditionalTime additionalTime = optionalAdditionalTime.get();
                    if (additionalTime.getStatus().equals(TimeStatus.RUNNING)) {
                        return additionalTime;
                    } else {
                        additionalTime.setStartedAt(new Date());
                        additionalTime.setStatus(TimeStatus.RUNNING);
                        return additionalTimeService.save(additionalTime);
                    }
                } else {
                    AdditionalTime additionalTime = new AdditionalTime(user, user.getRate(), new Date(), optionalWorkOrder.get(), user.getCompany(), true, TimeStatus.RUNNING);
                    return additionalTimeService.create(additionalTime);
                }
            } else {
                if (optionalAdditionalTime.isPresent()) {
                    AdditionalTime additionalTime = optionalAdditionalTime.get();
                    if (additionalTime.getStatus().equals(TimeStatus.STOPPED)) {
                        return additionalTime;
                    } else {
                        return additionalTimeService.stop(additionalTime);
                    }
                } else throw new CustomException("No timer to stop", HttpStatus.NOT_FOUND);
            }
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public AdditionalTime create(@ApiParam("AdditionalTime") @Valid @RequestBody AdditionalTime additionalTimeReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (additionalTimeService.canCreate(user, additionalTimeReq)
                && user.getCompany().getSubscription().getSubscriptionPlan().getFeatures().contains(PlanFeatures.ADDITIONAL_TIME)) {
            return additionalTimeService.create(additionalTimeReq);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AdditionalTime not found")})
    public AdditionalTime patch(@ApiParam("AdditionalTime") @Valid @RequestBody AdditionalTimePatchDTO additionalTime, @ApiParam("id") @PathVariable("id") Long id,
                                HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<AdditionalTime> optionalAdditionalTime = additionalTimeService.findById(id);

        if (optionalAdditionalTime.isPresent()) {
            AdditionalTime savedAdditionalTime = optionalAdditionalTime.get();
            if (additionalTimeService.hasAccess(user, savedAdditionalTime) && additionalTimeService.canPatch(user, additionalTime)) {
                return additionalTimeService.update(id, additionalTime);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("AdditionalTime not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AdditionalTime not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<AdditionalTime> optionalAdditionalTime = additionalTimeService.findById(id);
        if (optionalAdditionalTime.isPresent()) {
            AdditionalTime savedAdditionalTime = optionalAdditionalTime.get();
            if (additionalTimeService.hasAccess(user, savedAdditionalTime)) {
                additionalTimeService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("AdditionalTime not found", HttpStatus.NOT_FOUND);
    }
}
