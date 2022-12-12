package com.grash.controller;

import com.grash.dto.NotificationPatchDTO;
import com.grash.exception.CustomException;
import com.grash.model.Notification;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import com.grash.service.NotificationService;
import com.grash.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@Api(tags = "notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "NotificationCategory not found")})
    public Collection<Notification> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            return notificationService.findByUser(user.getId());
        } else return notificationService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Notification not found")})
    public Notification getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Notification> optionalNotification = notificationService.findById(id);
        if (optionalNotification.isPresent()) {
            Notification savedNotification = optionalNotification.get();
            if (notificationService.hasAccess(user, savedNotification)) {
                return savedNotification;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Notification not found")})
    public Notification patch(@ApiParam("Notification") @Valid @RequestBody NotificationPatchDTO notification, @ApiParam("id") @PathVariable("id") Long id,
                              HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Notification> optionalNotification = notificationService.findById(id);

        if (optionalNotification.isPresent()) {
            Notification savedNotification = optionalNotification.get();
            if (notificationService.hasAccess(user, savedNotification) && notificationService.canPatch(user, notification)) {
                return notificationService.update(id, notification);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Notification not found", HttpStatus.NOT_FOUND);
    }


}
