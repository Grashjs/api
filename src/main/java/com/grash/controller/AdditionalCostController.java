package com.grash.controller;

import com.grash.dto.AdditionalCostPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.AdditionalCost;
import com.grash.model.User;
import com.grash.model.WorkOrder;
import com.grash.service.AdditionalCostService;
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
import java.util.Optional;

@RestController
@RequestMapping("/additionalCosts")
@Api(tags = "additionalCost")
@RequiredArgsConstructor
public class AdditionalCostController {

    private final AdditionalCostService additionalCostService;
    private final UserService userService;
    private final WorkOrderService workOrderService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AdditionalCost not found")})
    public Optional<AdditionalCost> getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<AdditionalCost> optionalAdditionalCost = additionalCostService.findById(id);
        if (optionalAdditionalCost.isPresent()) {
            AdditionalCost savedAdditionalCost = optionalAdditionalCost.get();
            if (checkAccess(user, savedAdditionalCost)) {
                return optionalAdditionalCost;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else return null;
    }

    @PostMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public AdditionalCost create(@ApiParam("AdditionalCost") @RequestBody AdditionalCost additionalCostReq, HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(additionalCostReq.getWorkOrder().getId());
        if (optionalWorkOrder.isPresent()) {
            User workOrderCreator = userService.findById(optionalWorkOrder.get().getCreatedBy()).get();
            if (user.getCompany().getId().equals(workOrderCreator.getCompany().getId())) {
                return additionalCostService.create(additionalCostReq);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Invalid Work Order", HttpStatus.NOT_ACCEPTABLE);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AdditionalCost not found")})
    public AdditionalCost patch(@ApiParam("AdditionalCost") @RequestBody AdditionalCostPatchDTO additionalCost, @ApiParam("id") @PathVariable("id") Long id,
                                HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<AdditionalCost> optionalAdditionalCost = additionalCostService.findById(id);

        if (optionalAdditionalCost.isPresent()) {
            AdditionalCost savedAdditionalCost = optionalAdditionalCost.get();
            if (checkAccess(user, savedAdditionalCost)) {
                return additionalCostService.update(id, additionalCost);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("AdditionalCost not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "AdditionalCost not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);

        Optional<AdditionalCost> optionalAdditionalCost = additionalCostService.findById(id);
        if (optionalAdditionalCost.isPresent()) {
            AdditionalCost savedAdditionalCost = optionalAdditionalCost.get();
            if (checkAccess(user, savedAdditionalCost)) {
                additionalCostService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("AdditionalCost not found", HttpStatus.NOT_FOUND);
    }

    private boolean checkAccess(User user, AdditionalCost additionalCost) {
        return user.getCompany().getId().equals(
                userService.findById(additionalCost.getWorkOrder().getCreatedBy()).get().getCompany().getId());
    }
}
