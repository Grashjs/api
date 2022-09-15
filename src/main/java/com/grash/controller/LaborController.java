package com.grash.controller;

import com.grash.dto.LaborPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.Labor;
import com.grash.model.User;
import com.grash.model.WorkOrder;
import com.grash.model.enums.RoleType;
import com.grash.service.LaborService;
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
@RequestMapping("/labors")
@Api(tags = "labor")
@RequiredArgsConstructor
public class LaborController {

    private final LaborService laborService;
    private final UserService userService;
    private final WorkOrderService workOrderService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Labor not found")})
    public Labor getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<Labor> optionalLabor = laborService.findById(id);
        if (optionalLabor.isPresent()) {
            Labor savedLabor = optionalLabor.get();
            if (hasAccess(user, savedLabor)) {
                return optionalLabor.get();
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public Labor create(@ApiParam("Labor") @RequestBody Labor laborReq, HttpServletRequest req) {
        User user = userService.whoami(req);
        if (canCreate(user, laborReq)) {
            return laborService.create(laborReq);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Labor not found")})
    public Labor patch(@ApiParam("Labor") @RequestBody LaborPatchDTO labor, @ApiParam("id") @PathVariable("id") Long id,
                       HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<Labor> optionalLabor = laborService.findById(id);

        if (optionalLabor.isPresent()) {
            Labor savedLabor = optionalLabor.get();
            if (hasAccess(user, savedLabor)) {
                return laborService.update(id, labor);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Labor not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Labor not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);

        Optional<Labor> optionalLabor = laborService.findById(id);
        if (optionalLabor.isPresent()) {
            Labor savedLabor = optionalLabor.get();
            if (hasAccess(user, savedLabor)) {
                laborService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Labor not found", HttpStatus.NOT_FOUND);
    }

    private boolean hasAccess(User user, Labor labor) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(
                labor.getWorkOrder().getCompany().getId());
    }

    private boolean canCreate(User user, Labor laborReq) {
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(laborReq.getWorkOrder().getId());
        if (optionalWorkOrder.isPresent()) {
            return user.getCompany().getId().equals(optionalWorkOrder.get().getCompany().getId());
        } else throw new CustomException("Invalid Work order", HttpStatus.NOT_ACCEPTABLE);
    }
}
