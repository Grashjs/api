package com.grash.controller;

import com.grash.dto.AssetMiniDTO;
import com.grash.dto.AssetPatchDTO;
import com.grash.dto.AssetShowDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.AssetMapper;
import com.grash.model.Asset;
import com.grash.model.Location;
import com.grash.model.OwnUser;
import com.grash.model.Part;
import com.grash.model.enums.PermissionEntity;
import com.grash.model.enums.RoleType;
import com.grash.service.AssetService;
import com.grash.service.LocationService;
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
@RequestMapping("/assets")
@Api(tags = "asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;
    private final AssetMapper assetMapper;
    private final UserService userService;
    private final LocationService locationService;
    private final PartService partService;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public Collection<AssetShowDTO> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            if (user.getRole().getViewPermissions().contains(PermissionEntity.ASSETS)) {
                return assetService.findByCompany(user.getCompany().getId()).stream().filter(asset -> {
                    boolean canViewOthers = user.getRole().getViewOtherPermissions().contains(PermissionEntity.ASSETS);
                    return canViewOthers || asset.getCreatedBy().equals(user.getId());
                }).map(assetMapper::toShowDto).collect(Collectors.toList());
            } else throw new CustomException("Access Denied", HttpStatus.FORBIDDEN);
        } else return assetService.getAll().stream().map(assetMapper::toShowDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Asset not found")})
    public AssetShowDTO getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Asset> optionalAsset = assetService.findById(id);
        if (optionalAsset.isPresent()) {
            Asset savedAsset = optionalAsset.get();
            if (assetService.hasAccess(user, savedAsset) && user.getRole().getViewPermissions().contains(PermissionEntity.ASSETS) &&
                    (user.getRole().getViewOtherPermissions().contains(PermissionEntity.ASSETS) || savedAsset.getCreatedBy().equals(user.getId()))) {
                return assetMapper.toShowDto(savedAsset);
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/location/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Asset not found")})
    public Collection<AssetShowDTO> getByLocation(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Location> optionalLocation = locationService.findById(id);
        if (optionalLocation.isPresent() && locationService.hasAccess(user, optionalLocation.get())) {
            return assetService.findByLocation(id).stream().map(assetMapper::toShowDto).collect(Collectors.toList());
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }


    @GetMapping("/part/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Assets for this part not found")})
    public Collection<AssetShowDTO> getByPart(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Part> optionalPart = partService.findById(id);
        if (optionalPart.isPresent() && partService.hasAccess(user, optionalPart.get())) {
            return optionalPart.get().getAssets().stream().map(assetMapper::toShowDto).collect(Collectors.toList());
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/children/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Asset not found")})
    public Collection<AssetShowDTO> getChildrenById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (id.equals(0L) && user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            return assetService.findByCompany(user.getCompany().getId()).stream().filter(asset -> asset.getParentAsset() == null).map(assetMapper::toShowDto).collect(Collectors.toList());
        }
        Optional<Asset> optionalAsset = assetService.findById(id);
        if (optionalAsset.isPresent()) {
            Asset savedAsset = optionalAsset.get();
            if (assetService.hasAccess(user, savedAsset) && user.getRole().getViewPermissions().contains(PermissionEntity.ASSETS)) {
                return assetService.findAssetChildren(id).stream().map(assetMapper::toShowDto).collect(Collectors.toList());
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);

        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public AssetShowDTO create(@ApiParam("Asset") @Valid @RequestBody Asset assetReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (assetService.canCreate(user, assetReq) && user.getRole().getCreatePermissions().contains(PermissionEntity.ASSETS)) {
            if (assetReq.getParentAsset() != null) {
                Optional<Asset> optionalParentAsset = assetService.findById(assetReq.getParentAsset().getId());
                if (optionalParentAsset.isPresent()) {
                    Asset parentAsset = optionalParentAsset.get();
                    parentAsset.setHasChildren(true);
                    assetService.save(parentAsset);
                } else throw new CustomException("Parent Asset not found", HttpStatus.NOT_FOUND);

            }
            Asset createdAsset = assetService.create(assetReq);
            assetService.notify(createdAsset, "Asset " + createdAsset.getName() + " has been assigned to you");
            return assetMapper.toShowDto(createdAsset);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Asset not found")})
    public AssetShowDTO patch(@ApiParam("Asset") @Valid @RequestBody AssetPatchDTO asset, @ApiParam("id") @PathVariable("id") Long id,
                              HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Asset> optionalAsset = assetService.findById(id);

        if (optionalAsset.isPresent()) {
            Asset savedAsset = optionalAsset.get();
            if (assetService.hasAccess(user, savedAsset) && assetService.canPatch(user, asset)
                    && user.getRole().getEditOtherPermissions().contains(PermissionEntity.ASSETS) || savedAsset.getCreatedBy().equals(user.getId())
            ) {
                Asset patchedAsset = assetService.update(id, asset);
                assetService.patchNotify(savedAsset, patchedAsset);
                return assetMapper.toShowDto(patchedAsset);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Asset not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/mini")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
    })
    public Collection<AssetMiniDTO> getMini(HttpServletRequest req) {
        OwnUser asset = userService.whoami(req);
        return assetService.findByCompany(asset.getCompany().getId()).stream().map(assetMapper::toMiniDto).collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Asset not found")})
    public ResponseEntity<SuccessResponse> delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Asset> optionalAsset = assetService.findById(id);
        if (optionalAsset.isPresent()) {
            Asset savedAsset = optionalAsset.get();
            if (assetService.hasAccess(user, savedAsset) && (savedAsset.getCreatedBy().equals(user.getId()) ||
                    user.getRole().getDeleteOtherPermissions().contains(PermissionEntity.ASSETS))) {
                Asset parent = savedAsset.getParentAsset();
                assetService.delete(id);
                if (parent != null) {
                    Collection<Asset> siblings = assetService.findAssetChildren(parent.getId());
                    if (siblings.isEmpty()) {
                        parent.setHasChildren(false);
                        assetService.save(parent);
                    }
                }
                return new ResponseEntity<>(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Asset not found", HttpStatus.NOT_FOUND);
    }

}
