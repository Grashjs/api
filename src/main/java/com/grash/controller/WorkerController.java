package com.grash.controller;

import com.grash.exception.CustomException;
import com.grash.model.OwnUser;
import com.grash.model.abstracts.Worker;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleType;
import com.grash.service.UserService;
import com.grash.service.WorkerService;
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
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/workers")
@Api(tags = "worker")
@RequiredArgsConstructor
public class WorkerController {

    private final WorkerService workerService;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public Collection<Worker> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            if (user.getRole().getViewPermissions().contains(PermissionEntity.VENDORS_AND_CUSTOMERS)) {
                return workerService.findByCompany(user.getCompany().getId());
            } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        } else return workerService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Worker not found")})
    public Worker getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Worker> optionalWorker = workerService.findById(id);
        if (optionalWorker.isPresent()) {
            Worker savedWorker = optionalWorker.get();
            if (workerService.hasAccess(user, savedWorker) && user.getRole().getViewPermissions().contains(PermissionEntity.VENDORS_AND_CUSTOMERS)) {
                return savedWorker;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

}
