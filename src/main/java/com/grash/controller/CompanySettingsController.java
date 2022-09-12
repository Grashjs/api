package com.grash.controller;

import com.grash.exception.CustomException;
import com.grash.model.CompanySettings;
import com.grash.model.User;
import com.grash.model.enums.BasicPermission;
import com.grash.service.CompanySettingsService;
import com.grash.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/companySettings")
@Api(tags = "companySettings")
@RequiredArgsConstructor
public class CompanySettingsController {

    private final CompanySettingsService companySettingsService;

    private final UserService userService;

    private final ModelMapper modelMapper;

    @GetMapping("/{id}")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Activity not found")})
    public Optional<CompanySettings> getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        User user = userService.whoami(req);

        Optional<CompanySettings> companySettingsOptional = companySettingsService.findById(id);
        if (companySettingsOptional.isPresent()) {
            if (companySettingsOptional.get().getId().equals(user.getCompany().getCompanySettings().getId())) {
                return companySettingsService.findById(id);
            } else {
                throw new CustomException("Can't get someone else's companySettings", HttpStatus.NOT_ACCEPTABLE);
            }
        } else return null;
    }

    @PatchMapping("/{id}")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Application not found")})
    public CompanySettings patch(@ApiParam("CompanySettings") @RequestBody CompanySettings companySettings,
                                 HttpServletRequest req) {
        User user = userService.whoami(req);

        Optional<CompanySettings> companySettingsOptional = companySettingsService.findById(companySettings.getId());

        if (companySettingsOptional.isPresent()) {
            CompanySettings foundCompanySettings = companySettingsOptional.get();
            if (foundCompanySettings.getId().equals(user.getCompany().getCompanySettings().getId())) {
                if (user.getRole().getPermissions().contains(BasicPermission.ACCESS_SETTINGS)) {
                    return companySettingsService.update(companySettings);
                } else {
                    throw new CustomException("You don't have permission", HttpStatus.NOT_ACCEPTABLE);
                }
            } else {
                throw new CustomException("Can't get someone else's application", HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            throw new CustomException("CompanySettings not found", HttpStatus.NOT_FOUND);
        }
    }


}
