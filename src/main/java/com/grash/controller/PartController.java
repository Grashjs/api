package com.grash.controller;

import com.grash.dto.PartMiniDTO;
import com.grash.dto.PartPatchDTO;
import com.grash.dto.PartShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.PartMapper;
import com.grash.model.OwnUser;
import com.grash.model.Part;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleType;
import com.grash.service.PartService;
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
@RequestMapping("/parts")
@Api(tags = "part")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;
    private final PartMapper partMapper;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "PartCategory not found")})
    public Collection<PartShowDTO> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            if (user.getRole().getViewPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS)) {
                return partService.findByCompany(user.getCompany().getId()).stream().filter(part -> {
                    boolean canViewOthers = user.getRole().getViewOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS);
                    return canViewOthers || part.getCreatedBy().equals(user.getId());
                }).map(partMapper::toShowDto).collect(Collectors.toList());
            } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        } else return partService.getAll().stream().map(partMapper::toShowDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Part not found")})
    public PartShowDTO getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Part> optionalPart = partService.findById(id);
        if (optionalPart.isPresent()) {
            Part savedPart = optionalPart.get();
            if (partService.hasAccess(user, savedPart) && user.getRole().getViewPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS) &&
                    (user.getRole().getViewOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS) || savedPart.getCreatedBy().equals(user.getId()))) {
                return partMapper.toShowDto(savedPart);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public PartShowDTO create(@ApiParam("Part") @Valid @RequestBody Part partReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (partService.canCreate(user, partReq) && user.getRole().getCreatePermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS)) {
            Part savedPart = partService.create(partReq);
            partService.notify(savedPart);
            return partMapper.toShowDto(savedPart);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Part not found")})
    public PartShowDTO patch(@ApiParam("Part") @Valid @RequestBody PartPatchDTO part, @ApiParam("id") @PathVariable("id") Long id,
                             HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Part> optionalPart = partService.findById(id);

        if (optionalPart.isPresent()) {
            Part savedPart = optionalPart.get();
            if (partService.hasAccess(user, savedPart) && partService.canPatch(user, part)
                    && user.getRole().getEditOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS) || savedPart.getCreatedBy().equals(user.getId())) {
                Part patchedPart = partService.update(id, part);
                partService.patchNotify(savedPart, patchedPart);
                return partMapper.toShowDto(patchedPart);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Part not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/mini")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public Collection<PartMiniDTO> getMini(HttpServletRequest req) {
        OwnUser part = userService.whoami(req);
        return partService.findByCompany(part.getCompany().getId()).stream().map(partMapper::toMiniDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Part not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Part> optionalPart = partService.findById(id);
        if (optionalPart.isPresent()) {
            Part savedPart = optionalPart.get();
            if (partService.hasAccess(user, savedPart)
                    && (savedPart.getId().equals(user.getId()) || user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS))) {
                partService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Part not found", HttpStatus.NOT_FOUND);
    }

}
