package com.grash.controller;

import com.grash.exception.CustomException;
import com.grash.model.User;
import com.grash.model.WorkOrderHistory;
import com.grash.model.enums.RoleType;
import com.grash.service.UserService;
import com.grash.service.WorkOrderHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/work-order-histories")
@Api(tags = "workOrderHistory")
@RequiredArgsConstructor
public class WorkOrderHistoryController {

    private final WorkOrderHistoryService workOrderHistoryService;
    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "WorkOrderHistory not found")})
    public WorkOrderHistory getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);
        Optional<WorkOrderHistory> optionalWorkOrderHistory = workOrderHistoryService.findById(id);
        if (optionalWorkOrderHistory.isPresent()) {
            WorkOrderHistory savedWorkOrderHistory = optionalWorkOrderHistory.get();
            if (hasAccess(user, savedWorkOrderHistory)) {
                return savedWorkOrderHistory;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }


    private boolean hasAccess(User user, WorkOrderHistory workOrderHistory) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workOrderHistory.getWorkOrder().getCompany().getId());
    }

}
