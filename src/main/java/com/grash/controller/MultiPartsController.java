package com.grash.controller;

import com.grash.dto.MultiPartsPatchDTO;
import com.grash.dto.MultiPartsShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.MultiPartsMapper;
import com.grash.model.MultiParts;
import com.grash.model.OwnUser;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleType;
import com.grash.service.MultiPartsService;
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
@RequestMapping("/multi-parts")
@Api(tags = "multiParts")
@RequiredArgsConstructor
public class MultiPartsController {

    private final MultiPartsService multiPartsService;
    private final MultiPartsMapper multiPartsMapper;
    private final UserService userService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "MultiPartsCategory not found")})
    public Collection<MultiPartsShowDTO> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            return multiPartsService.findByCompany(user.getCompany().getId()).stream().map(multiPartsMapper::toShowDto).collect(Collectors.toList());
        } else return multiPartsService.getAll().stream().map(multiPartsMapper::toShowDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "MultiParts not found")})
    public MultiPartsShowDTO getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<MultiParts> optionalMultiParts = multiPartsService.findById(id);
        if (optionalMultiParts.isPresent()) {
            MultiParts savedMultiParts = optionalMultiParts.get();
            if (multiPartsService.hasAccess(user, savedMultiParts)) {
                return multiPartsMapper.toShowDto(savedMultiParts);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public MultiPartsShowDTO create(@ApiParam("MultiParts") @Valid @RequestBody MultiParts multiPartsReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (multiPartsService.canCreate(user, multiPartsReq)) {
            return multiPartsMapper.toShowDto(multiPartsService.create(multiPartsReq));
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "MultiParts not found")})
    public MultiPartsShowDTO patch(@ApiParam("MultiParts") @Valid @RequestBody MultiPartsPatchDTO multiParts, @ApiParam("id") @PathVariable("id") Long id,
                                   HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<MultiParts> optionalMultiParts = multiPartsService.findById(id);

        if (optionalMultiParts.isPresent()) {
            MultiParts savedMultiParts = optionalMultiParts.get();
            if (multiPartsService.hasAccess(user, savedMultiParts) && multiPartsService.canPatch(user, multiParts)) {
                return multiPartsMapper.toShowDto(multiPartsService.update(id, multiParts));
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("MultiParts not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "MultiParts not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<MultiParts> optionalMultiParts = multiPartsService.findById(id);
        if (optionalMultiParts.isPresent()) {
            MultiParts savedMultiParts = optionalMultiParts.get();
            if (multiPartsService.hasAccess(user, savedMultiParts)
                    && user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.PARTS_AND_MULTIPARTS)) {
                multiPartsService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("MultiParts not found", HttpStatus.NOT_FOUND);
    }

}
