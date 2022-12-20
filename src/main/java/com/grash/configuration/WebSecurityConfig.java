package com.grash.configuration;

import com.grash.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Value("${client.post-logout-uri}")
    private String postLogoutUri;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // Disable CSRF (cross site request forgery)
        http.csrf().disable();

        // No session will be created or used by spring security
        //http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // Entry points
        http.authorizeRequests()//
                .antMatchers("/auth/login").permitAll()
                .antMatchers("/auth2/login")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .oauth2Login()
                .loginPage("/auth2/login")
                .defaultSuccessUrl("/auth2/login-success", true)
                .failureUrl("/auth2/login-failure")
                .and()
                .logout().logoutUrl("/logout")
                .logoutSuccessHandler(oidcLogoutSuccessHandler());
//                .antMatchers("/").permitAll()
//                .antMatchers("auth/login").permitAll()
//                .antMatchers("/checklists").permitAll()
//                .antMatchers("/auth/signin").permitAll()//
//                .antMatchers("/auth/signin").permitAll()//
//                .antMatchers("/auth/signup").permitAll()//
//                .antMatchers("/auth/sendMail").permitAll()//
//                .antMatchers("/auth/resetpwd/**").permitAll()
//                .antMatchers("/mail/send").permitAll()
//                .antMatchers(HttpMethod.POST, "/newsLetters").permitAll()
//                .antMatchers("/auth/activate-account**").permitAll()//
//                .antMatchers("/h2-console/**/**").permitAll()
//                // Disallow everything else..
//                .anyRequest().authenticated()
//                .and()
//                .oauth2Login().loginPage("/auth/login");

        // If a user try to access a resource without having enough permissions
        http.exceptionHandling().accessDeniedPage("/auth2/login");

        // Apply JWT
        // http.apply(new JwtTokenFilterConfigurer(jwtTokenProvider));
        http.cors();

        // Optional, if you want to test the API from a browser
        // http.httpBasic();
    }

    @Override
    public void configure(WebSecurity web) {
        // Allow swagger to be accessed without authentication
        web.ignoring().antMatchers("/v2/api-docs")//
                .antMatchers("/swagger-resources/**")//
                .antMatchers("/swagger-ui.html")//
                .antMatchers("/com/grash/configuration/**")//
                .antMatchers("/webjars/**")//
                .antMatchers("/public")

                // Un-secure H2 Database (for testing purposes, H2 console shouldn't be unprotected in production)
                .and()
                .ignoring()
                .antMatchers("/h2-console/**/**");
        ;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * Handles OIDC Logout redirection.
     *
     * @return LogoutSuccessHandler
     */
    private LogoutSuccessHandler oidcLogoutSuccessHandler() {

        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(
                this.clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(URI.create(postLogoutUri));
        return oidcLogoutSuccessHandler;
    }
}
