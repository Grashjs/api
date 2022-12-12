package com.grash.controller;

import com.grash.dto.MeterPatchDTO;
import com.grash.dto.MeterShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.MeterMapper;
import com.grash.model.Asset;
import com.grash.model.Meter;
import com.grash.model.OwnUser;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.PlanFeatures;
import com.grash.model.enums.RoleType;
import com.grash.service.AssetService;
import com.grash.service.MeterService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/meters")
@Api(tags = "meter")
@RequiredArgsConstructor
public class MeterController {

    private final MeterService meterService;
    private final MeterMapper meterMapper;
    private final UserService userService;
    private final AssetService assetService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "MeterCategory not found")})
    public Collection<MeterShowDTO> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            if (user.getRole().getViewPermissions().contains(PermissionEntity.METERS)) {
                return meterService.findByCompany(user.getCompany().getId()).stream().filter(meter -> {
                    boolean canViewOthers = user.getRole().getViewOtherPermissions().contains(PermissionEntity.METERS);
                    return canViewOthers || meter.getCreatedBy().equals(user.getId());
                }).map(meterMapper::toShowDto).collect(Collectors.toList());
            } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        } else return meterService.getAll().stream().map(meterMapper::toShowDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Meter not found")})
    public MeterShowDTO getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Meter> optionalMeter = meterService.findById(id);
        if (optionalMeter.isPresent()) {
            Meter savedMeter = optionalMeter.get();
            if (meterService.hasAccess(user, savedMeter) && user.getRole().getViewPermissions().contains(PermissionEntity.METERS) &&
                    (user.getRole().getViewOtherPermissions().contains(PermissionEntity.METERS) || savedMeter.getCreatedBy().equals(user.getId()))) {
                return meterMapper.toShowDto(savedMeter);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public MeterShowDTO create(@ApiParam("Meter") @Valid @RequestBody Meter meterReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (meterService.canCreate(user, meterReq) && user.getRole().getCreatePermissions().contains(PermissionEntity.METERS)
                && user.getCompany().getSubscription().getSubscriptionPlan().getFeatures().contains(PlanFeatures.METER)) {
            Meter savedMeter = meterService.create(meterReq);
            meterService.notify(savedMeter);
            return meterMapper.toShowDto(savedMeter);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Meter not found")})
    public MeterShowDTO patch(@ApiParam("Meter") @Valid @RequestBody MeterPatchDTO meter, @ApiParam("id") @PathVariable("id") Long id,
                              HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Meter> optionalMeter = meterService.findById(id);

        if (optionalMeter.isPresent()) {
            Meter savedMeter = optionalMeter.get();
            if (meterService.hasAccess(user, savedMeter) && meterService.canPatch(user, meter)
                    && user.getRole().getEditOtherPermissions().contains(PermissionEntity.METERS) || savedMeter.getCreatedBy().equals(user.getId())) {
                Meter patchedMeter = meterService.update(id, meter);
                meterService.patchNotify(savedMeter, patchedMeter);
                return meterMapper.toShowDto(patchedMeter);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Meter not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/asset/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "WorkOrderHistory not found")})
    public Collection<MeterShowDTO> getByAsset(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Asset> optionalAsset = assetService.findById(id);
        if (optionalAsset.isPresent() && assetService.hasAccess(user, optionalAsset.get())) {
            return meterService.findByAsset(id).stream().map(meterMapper::toShowDto).collect(Collectors.toList());
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Meter not found")})
    public ResponseEntity<SuccessResponse> delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Meter> optionalMeter = meterService.findById(id);
        if (optionalMeter.isPresent()) {
            Meter savedMeter = optionalMeter.get();
            if (meterService.hasAccess(user, savedMeter)
                    && (savedMeter.getCreatedBy().equals(user.getId()) ||
                    user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.METERS))) {
                meterService.delete(id);
                return new ResponseEntity<>(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Meter not found", HttpStatus.NOT_FOUND);
    }

}
