package com.grash.security.oauth2;

import com.grash.dto.UserSignupRequest;
import com.grash.exception.CustomException;
import com.grash.model.OwnUser;
import com.grash.model.enums.AuthProvider;
import com.grash.repository.UserRepository;
import com.grash.security.oauth2.user.OAuth2UserInfo;
import com.grash.security.oauth2.user.OAuth2UserInfoFactory;
import com.grash.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private UserService userService;

    @Autowired
    public void setUserService(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest oidcUserRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(oidcUserRequest);

        try {
            return processOAuth2User(oidcUserRequest, oidcUser);
        } catch (AuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            // Throwing an instance of AuthenticationException will trigger the OAuth2AuthenticationFailureHandler
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OidcUser processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OidcUser oidcUser) {
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.getClientRegistration().getRegistrationId(), oidcUser.getAttributes());
        if (StringUtils.isEmpty(oAuth2UserInfo.getEmail())) {
            throw new CustomException("Email not found from OAuth2 provider", HttpStatus.FORBIDDEN);
        }
        OwnUser user;
        if (userRepository.existsByEmail(oAuth2UserInfo.getEmail())) {
            user = userRepository.findUserByEmail(oAuth2UserInfo.getEmail());
            if (!user.getProvider().equals(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()))) {
                throw new CustomException("Looks like you're signed up with " +
                        user.getProvider() + " account. Please use your " + user.getProvider() +
                        " account to login.", HttpStatus.FORBIDDEN);
            }
            updateExistingUser(user, oAuth2UserInfo);


        } else {
            registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }
        return oidcUser;

    }

    private OwnUser registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        Map<String, Object> attributes = oAuth2UserInfo.getAttributes();
        UserSignupRequest signupRequest = UserSignupRequest.builder()
                .firstName(oAuth2UserInfo.getName())
                .lastName(oAuth2UserInfo.getName())
                .email(oAuth2UserInfo.getEmail())
                .build();
        return userService.signup(signupRequest, AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId()), oAuth2UserInfo.getId());
    }

    private OwnUser updateExistingUser(OwnUser existingUser, OAuth2UserInfo oAuth2UserInfo) {
        existingUser.setFirstName(oAuth2UserInfo.getName());
        return userRepository.save(existingUser);
    }

}
