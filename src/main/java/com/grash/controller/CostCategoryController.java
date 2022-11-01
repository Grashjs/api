package com.grash.controller;

import com.grash.dto.CategoryPatchDTO;
import com.grash.dto.CategoryPostDTO;
import com.grash.dto.SuccessResponse;
import com.grash.exception.CustomException;
import com.grash.mapper.CostCategoryMapper;
import com.grash.model.CompanySettings;
import com.grash.model.CostCategory;
import com.grash.model.OwnUser;
import com.grash.model.enums.BasicPermission;
import com.grash.model.enums.RoleType;
import com.grash.service.CostCategoryService;
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
@RequestMapping("/cost-categories")
@Api(tags = "costCategory")
@RequiredArgsConstructor
public class CostCategoryController {

    private final CostCategoryService costCategoryService;
    private final UserService userService;
    private final CostCategoryMapper costCategoryMapper;

    @GetMapping("")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "AssetCategory not found")})
    public Collection<CostCategory> getAll(HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getRoleType().equals(RoleType.ROLE_CLIENT)) {
            CompanySettings companySettings = user.getCompany().getCompanySettings();
            return costCategoryService.findByCompanySettings(companySettings.getId());
        } else return costCategoryService.getAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "CostCategory not found")})
    public CostCategory getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<CostCategory> costCategoryOptional = costCategoryService.findById(id);
        if (costCategoryOptional.isPresent()) {
            CostCategory costCategory = costCategoryOptional.get();
            if (costCategoryService.hasAccess(user, costCategory)) {
                return costCategoryService.findById(id).get();
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public CostCategory create(@ApiParam("CostCategory") @Valid @RequestBody CategoryPostDTO costCategoryReq, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (user.getRole().getPermissions().contains(BasicPermission.CREATE_EDIT_CATEGORIES)) {
            CostCategory costCategory = costCategoryMapper.toModel(costCategoryReq);
            costCategory.setCompanySettings(user.getCompany().getCompanySettings());
            return costCategoryService.create(costCategory);
        } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "CostCategory not found")})
    public CostCategory patch(@ApiParam("CostCategory") @Valid @RequestBody CategoryPatchDTO costCategory, @ApiParam("id") @PathVariable("id") Long id,
                              HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        if (costCategoryService.findById(id).isPresent()) {
            CostCategory savedCostCategory = costCategoryService.findById(id).get();
            if (user.getRole().getPermissions().contains(BasicPermission.CREATE_EDIT_CATEGORIES) &&
                    user.getRole().getCompanySettings().getId().equals(savedCostCategory.getCompanySettings().getId())) {
                return costCategoryService.update(id, costCategory);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("CostCategory not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "CostCategory not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<CostCategory> optionalCostCategory = costCategoryService.findById(id);
        if (optionalCostCategory.isPresent()) {
            if (user.getCompany().getCompanySettings().getId().equals(optionalCostCategory.get().getCompanySettings().getId())
                    && user.getRole().getPermissions().contains(BasicPermission.DELETE_CATEGORIES)) {
                costCategoryService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("CostCategory not found", HttpStatus.NOT_FOUND);
    }
}
