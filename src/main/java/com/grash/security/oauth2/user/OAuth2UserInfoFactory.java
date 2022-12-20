package com.grash.security.oauth2.user;

import com.grash.exception.CustomException;
import com.grash.model.enums.AuthProvider;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if (registrationId.equalsIgnoreCase(AuthProvider.google.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(AuthProvider.wso2.toString())) {
            return new Wso2OAuth2UserInfo(attributes);
        } else {
            throw new CustomException("Sorry! Login with " + registrationId + " is not supported yet.", HttpStatus.FORBIDDEN);
        }
    }
}
