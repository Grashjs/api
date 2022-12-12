package com.grash.controller;

import com.grash.dto.FloorPlanPatchDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.model.FloorPlan;
import com.grash.model.Location;
import com.grash.model.OwnUser;
import com.grash.service.FloorPlanService;
import com.grash.service.LocationService;
import com.grash.service.UserService;
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
import java.util.Optional;

@RestController
@RequestMapping("/floor-plans")
@Api(tags = "floorPlan")
@RequiredArgsConstructor
public class FloorPlanController {

    private final FloorPlanService floorPlanService;
    private final UserService userService;
    private final LocationService locationService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "FloorPlan not found")})
    public FloorPlan getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<FloorPlan> optionalFloorPlan = floorPlanService.findById(id);
        if (optionalFloorPlan.isPresent()) {
            FloorPlan savedFloorPlan = optionalFloorPlan.get();
            if (floorPlanService.hasAccess(user, savedFloorPlan)) {
                return savedFloorPlan;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/location/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "FloorPlan not found")})
    public Collection<FloorPlan> getByLocation(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Location> optionalLocation = locationService.findById(id);
        if (optionalLocation.isPresent() && locationService.hasAccess(user, optionalLocation.get())) {
            return floorPlanService.findByLocation(id);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public FloorPlan create(@ApiParam("FloorPlan") @Valid @RequestBody FloorPlan floorPlanReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (floorPlanService.canCreate(user, floorPlanReq)) {
            return floorPlanService.create(floorPlanReq);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "FloorPlan not found")})
    public FloorPlan patch(@ApiParam("FloorPlan") @Valid @RequestBody FloorPlanPatchDTO floorPlan, @ApiParam("id") @PathVariable("id") Long id,
                           HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<FloorPlan> optionalFloorPlan = floorPlanService.findById(id);

        if (optionalFloorPlan.isPresent()) {
            FloorPlan savedFloorPlan = optionalFloorPlan.get();
            if (floorPlanService.hasAccess(user, savedFloorPlan) && floorPlanService.canPatch(user, floorPlan)) {
                return floorPlanService.update(id, floorPlan);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("FloorPlan not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "FloorPlan not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<FloorPlan> optionalFloorPlan = floorPlanService.findById(id);
        if (optionalFloorPlan.isPresent()) {
            FloorPlan savedFloorPlan = optionalFloorPlan.get();
            if (floorPlanService.hasAccess(user, savedFloorPlan)) {
                floorPlanService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("FloorPlan not found", HttpStatus.NOT_FOUND);
    }

}
