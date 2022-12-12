package com.grash.controller;

import com.grash.dto.CompanyPatchDTO;
import com.grash.exception.CustomException;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.service.CompanyService;
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
import java.util.Optional;

@RestController
@RequestMapping("/companies")
@Api(tags = "company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    private final UserService userService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Company not found")})
    public Company getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Company> companyOptional = companyService.findById(id);
        if (companyOptional.isPresent()) {
            if (companyService.hasAccess(user, companyOptional.get())) {
                return companyService.findById(id).get();
            } else {
                throw new CustomException("Can't get someone else's company", HttpStatus.NOT_ACCEPTABLE);
            }
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Company not found")})
    public Company patch(@ApiParam("Company") @Valid @RequestBody CompanyPatchDTO company, @ApiParam("id") @PathVariable("id") Long id,
                         HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Company> optionalCompany = companyService.findById(id);

        if (optionalCompany.isPresent()) {
            Company savedCompany = optionalCompany.get();
            if (companyService.hasAccess(user, savedCompany) && companyService.canPatch(user, company)) {
                return companyService.update(id, company);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Company not found", HttpStatus.NOT_FOUND);
    }

}
