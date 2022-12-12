package com.grash.controller;

import com.grash.dto.*;
import com.grash.exception.CustomException;
import com.grash.mapper.UserMapper;
import com.grash.model.OwnUser;
import com.grash.service.EmailService2;
import com.grash.service.UserService;
import com.grash.service.VerificationTokenService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Api(tags = "auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final UserMapper userMapper;
    private final EmailService2 emailService2;
    @Value("${frontend.url}")
    private String frontendUrl;

//    @GetMapping("/signin")
//    public String getLoginPage(@RequestBody UserLoginRequest userLoginRequest) {
//        return "redirect:/oauth2/authorization/wso2";
//    }

    @PostMapping(
            path = "/login",
            produces = {
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    @ApiOperation(value = "${AuthController.signin}")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Something went wrong"),
            @ApiResponse(code = 422, message = "Invalid credentials")
    })
    public String login(
            @ApiParam("AuthLoginRequest") @Valid @RequestBody UserLoginRequest userLoginRequest) {
        return "redirect:/oauth2/authorization/wso2";
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
        return userService.signup(user);
    }

    @PostMapping(
            path = "/sendMail",
            produces = "text/html;charset=UTF-8"
    )
    @ApiOperation(value = "${AuthController.signup}")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 422, message = "Username is already in use")})
    public void sendMail(@ApiParam("Signup User") @Valid @RequestBody UserSignupRequest user) {
        String email = "ibracool99@gmail.com";
        String subject = "GG";
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("verifyTokenLink", "gg");
            put("featuresLink", "s");
        }};
        try {
            emailService2.sendMessageUsingThymeleafTemplate(email, subject, variables, "signup.html");
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("Couldn't send email", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/activate-account")
    @ApiOperation(value = "activate account")
    @ApiResponses(value = {//
            @ApiResponse(code = 400, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public void activateAcount(
            @ApiParam("token") @RequestParam String token, HttpServletResponse httpServletResponse
    ) throws Exception {
        try {
            verificationTokenService.confirmMail(token);
            httpServletResponse.setHeader("Location", frontendUrl + "/account/login");
        } catch (Exception ex) {
            httpServletResponse.setHeader("Location", frontendUrl + "/signup");
        }
        httpServletResponse.setStatus(302);
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

    @PreAuthorize("permitAll()")
    @PostMapping(value = "/updatepwd", produces = "application/json")
    public ResponseEntity<SuccessResponse> updatePassword(@Valid @RequestBody UpdatePasswordRequest updatePasswordRequest, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        String password = user.getPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();
        if (passwordEncoder.matches(oldPassword, password)) {
            user.setPassword(passwordEncoder.encode(updatePasswordRequest.getNewPassword()));
            userService.save(user);
            return ResponseEntity.ok(new SuccessResponse(true, "Password changed successfully"));
        } else {
            return new ResponseEntity(new SuccessResponse(false, "Bad credentials"),
                    HttpStatus.NOT_ACCEPTABLE);
        }
    }
}
