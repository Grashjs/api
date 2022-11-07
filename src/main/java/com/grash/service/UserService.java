package com.grash.service;

import com.grash.dto.SuccessResponse;
import com.grash.dto.UserSignupRequest;
import com.grash.exception.CustomException;
import com.grash.mapper.UserMapper;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.Role;
import com.grash.model.VerificationToken;
import com.grash.repository.UserRepository;
import com.grash.repository.VerificationTokenRepository;
import com.grash.security.JwtTokenProvider;
import com.grash.utils.Helper;
import com.grash.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.*;


@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final Utils utils;
    private final EmailService emailService;
    private final RoleService roleService;
    private final CompanyService companyService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserMapper userMapper;

    @Value("${api.host}")
    private String API_HOST;

    public String signin(String email, String password, String type) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + type.toUpperCase()))) {
                throw new CustomException("Invalid credentials", HttpStatus.FORBIDDEN);
            }
            return jwtTokenProvider.createToken(email, Arrays.asList(userRepository.findUserByEmail(email).getRole().getRoleType()));
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid credentials", HttpStatus.FORBIDDEN);
        }
    }

    public SuccessResponse signup(OwnUser user) {
        if (!userRepository.existsByEmail(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setUsername(utils.generateStringId());
            if (user.getRole() == null) {
                //create company with default roles
                Company company = new Company();
                companyService.create(company);
                user.setOwnsCompany(true);
                user.setCompany(company);
                user.setRole(company.getCompanySettings().getRoleList().stream().filter(role -> role.getName().equals("Administrator")).findFirst().get());
            } else {
                Optional<Role> optionalRole = roleService.findById(user.getRole().getId());
                if (optionalRole.isPresent()) {
                    user.setRole(optionalRole.get());
                    if (optionalRole.get().getCompanySettings() != null) {
                        user.setCompany(optionalRole.get().getCompanySettings().getCompany());
                    }
                } else throw new CustomException("Role not found", HttpStatus.NOT_ACCEPTABLE);
            }
            if (API_HOST.equals("http://localhost:8080")) {
                user.setEnabled(true);
                userRepository.save(user);
                return new SuccessResponse(true, jwtTokenProvider.createToken(user.getEmail(), Arrays.asList(user.getRole().getRoleType())));
            } else {
                /*
                //send mail
                String token = UUID.randomUUID().toString();
                String link = API_HOST + "/auth/activate-account?token=" + token;

                emailService.send(user.getEmail(), "Email de confirmation", emailService.buildEmail("Confirmation", "Confirmez votre  adresse mail", link));

                VerificationToken newUserToken = new VerificationToken(token, user);
                verificationTokenRepository.save(newUserToken);
                userRepository.save(user);

                return new SuccessResponse(true, "Successful registration. Check your mailbox to activate your account");
                 */
                user.setEnabled(true);
                userRepository.save(user);
                return new SuccessResponse(true, jwtTokenProvider.createToken(user.getEmail(), Arrays.asList(user.getRole().getRoleType())));
            }
        } else {
            throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    public OwnUser search(String email) {
        OwnUser user = userRepository.findUserByEmail(email);
        if (user == null) {
            throw new CustomException("The user doesn't exist", HttpStatus.NOT_FOUND);
        }
        return user;
    }

    public OwnUser whoami(HttpServletRequest req) {
        return userRepository.findUserByEmail(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req)));
    }

    public String refresh(String username) {
        return jwtTokenProvider.createToken(username, Arrays.asList(userRepository.findUserByEmail(username).getRole().getRoleType()));
    }

    public List<OwnUser> getAll() {
        return userRepository.findAll();
    }

    public long count() {
        return userRepository.count();
    }

    public Optional<OwnUser> findById(Long id) {
        return userRepository.findById(id);
    }

    public OwnUser update(Long id, UserSignupRequest user) {
        if (userRepository.existsById(id)) {
            OwnUser savedUser = userRepository.findById(id).get();
            return userRepository.save(userMapper.updateUser(savedUser, user));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public void enableUser(String email) {
        OwnUser user = userRepository.findUserByEmail(email);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public SuccessResponse resetPassword(String email) {
        OwnUser user = search(email);
        Helper helper = new Helper();
        String password = helper.generateString().replace("-", "").substring(0, 8).toUpperCase();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        String message = "Votre mot de passe a été réinitialisé.\nVeuillez vous reconnecter en utilisant ce mot de passe: " + password +
                "\nIl est important procéder au changement de votre mot de passe le plutôt possible pour des raisons de sécurité.";
        emailService.send(user.getEmail(), "Réinitialisation de votre mot de passe Sutura", message);
        return new SuccessResponse(true, "Password changed successfully");
    }

    public Collection<OwnUser> findByCompany(Long id) {
        return userRepository.findByCompany_Id(id);
    }
}
