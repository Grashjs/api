package com.grash.controller;

import com.grash.dto.*;
import com.grash.mapper.UserMapper;
import com.grash.service.UserService;
import com.grash.service.VerificationTokenService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
@Api(tags = "auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final UserMapper userMapper;

    @PostMapping(
            path = "/signin",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @ApiOperation(value = "${AuthController.signin}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(
            @ApiParam("AuthLoginRequest") @Valid @RequestBody UserLoginRequest userLoginRequest) {
        AuthResponse authResponse = new AuthResponse(userService.signin(userLoginRequest.getEmail(), userLoginRequest.getPassword(), userLoginRequest.getType()));
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping(
            path = "/signup",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            })
    @ApiOperation(value = "${AuthController.signup}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use")})
    public SuccessResponse signup(@ApiParam("Signup User") @Valid @RequestBody UserSignupRequest user) {
        return userService.signup(userMapper.toModel(user));
    }

    @GetMapping("/activate-account")
    @ApiOperation(value = "activate account")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public AuthResponse activateAcount(
            @ApiParam("token") @RequestParam String token
    ) throws Exception {
        return verificationTokenService.confirmMail(token);
    }

    @DeleteMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @ApiOperation(value = "${AuthController.delete}", authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "The user doesn't exist"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public String delete(@ApiParam("Username") @PathVariable String username) {
        userService.delete(username);
        return username;
    }

    @GetMapping(value = "/{username}")
    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @ApiOperation(value = "${AuthController.search}", response = UserResponseDTO.class, authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "The user doesn't exist"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserResponseDTO search(@ApiParam("Username") @PathVariable String username) {
        return userMapper.toDto(userService.search(username));
    }

    @GetMapping(value = "/me")
    @PreAuthorize("permitAll()")
    @ApiOperation(value = "${AuthController.me}", response = UserResponseDTO.class, authorizations = {@Authorization(value = "apiKey")})
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 500, message = "Expired or invalid JWT token")})
    public UserResponseDTO whoami(HttpServletRequest req) {
        return userMapper.toDto(userService.whoami(req));
    }

    @GetMapping("/refresh")
    @PreAuthorize("permitAll()")
    public AuthResponse refresh(HttpServletRequest req) {
        return new AuthResponse(userService.refresh(req.getRemoteUser()));
    }

    @PreAuthorize("permitAll()")
    @GetMapping(value = "/resetpwd", produces = "application/json")
    public SuccessResponse resetPassword(@RequestParam String email) {
        return userService.resetPassword(email);
    }
}
