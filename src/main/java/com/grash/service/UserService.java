package com.grash.service;

import com.grash.advancedsearch.SearchCriteria;
import com.grash.advancedsearch.SpecificationBuilder;
import com.grash.dto.SuccessResponse;
import com.grash.dto.UserPatchDTO;
import com.grash.dto.UserSignupRequest;
import com.grash.exception.CustomException;
import com.grash.mapper.UserMapper;
import com.grash.model.*;
import com.grash.repository.UserRepository;
import com.grash.repository.VerificationTokenRepository;
import com.grash.security.JwtTokenProvider;
import com.grash.utils.Helper;
import com.grash.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
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
    private final EntityManager em;
    private final AuthenticationManager authenticationManager;
    private final Utils utils;
    private final MessageSource messageSource;
    private final EmailService2 emailService2;
    private final RoleService roleService;
    private final CompanyService companyService;
    private final CurrencyService currencyService;
    private final UserInvitationService userInvitationService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionService subscriptionService;
    private final UserMapper userMapper;

    @Value("${api.host}")
    private String API_HOST;
    @Value("${frontend.url}")
    private String frontendUrl;

    public String signin(String email, String password, String type) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            if (authentication.getAuthorities().stream().noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + type.toUpperCase()))) {
                throw new CustomException("Invalid credentials", HttpStatus.FORBIDDEN);
            }
            Optional<OwnUser> optionalUser = userRepository.findByEmail(email);
            OwnUser user = optionalUser.get();
            user.setLastLogin(new Date());
            userRepository.save(user);
            return jwtTokenProvider.createToken(email, Collections.singletonList(user.getRole().getRoleType()));
        } catch (AuthenticationException e) {
            throw new CustomException("Invalid credentials", HttpStatus.FORBIDDEN);
        }
    }

    public SuccessResponse signup(UserSignupRequest userReq) {
        OwnUser user = userMapper.toModel(userReq);
        if (!userRepository.existsByEmail(user.getEmail())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setUsername(utils.generateStringId());
            if (user.getRole() == null) {
                //create company with default roles
                Subscription subscription = Subscription.builder().usersCount(3).monthly(true)
                        .startsOn(new Date())
                        .endsOn(Helper.incrementDays(new Date(), 15))
                        .subscriptionPlan(subscriptionPlanService.findByCode("BUSINESS").get()).build();
                subscriptionService.create(subscription);
                Company company = new Company(userReq.getCompanyName(), userReq.getEmployeesCount(), subscription);
                company.getCompanySettings().getGeneralPreferences().setCurrency(currencyService.findByCode("$").get());
                companyService.create(company);
                user.setOwnsCompany(true);
                user.setCompany(company);
                user.setRole(company.getCompanySettings().getRoleList().stream().filter(role -> role.getName().equals("Administrator")).findFirst().get());
            } else {
                Optional<Role> optionalRole = roleService.findById(user.getRole().getId());
                if (optionalRole.isPresent()) {
                    if (userInvitationService.findByRoleAndEmail(optionalRole.get().getId(), user.getEmail()).isEmpty()) {
                        throw new CustomException("You are not invited to this organization for this role", HttpStatus.NOT_ACCEPTABLE);
                    } else {
                        user.setRole(optionalRole.get());
                        user.setEnabled(true);
                        user.setCompany(optionalRole.get().getCompanySettings().getCompany());
                    }

                } else throw new CustomException("Role not found", HttpStatus.NOT_ACCEPTABLE);
            }
            if (API_HOST.equals("http://localhost:8080")) {
                user.setEnabled(true);
                userRepository.save(user);
                return new SuccessResponse(true, jwtTokenProvider.createToken(user.getEmail(), Collections.singletonList(user.getRole().getRoleType())));
            } else {
                if (user.getRole() == null) { //send mail
                    String token = UUID.randomUUID().toString();
                    String link = API_HOST + "/auth/activate-account?token=" + token;
                    Map<String, Object> variables = new HashMap<String, Object>() {{
                        put("verifyTokenLink", link);
                        put("featuresLink", frontendUrl + "/#key-features");
                    }};
                    VerificationToken newUserToken = new VerificationToken(token, user);
                    verificationTokenRepository.save(newUserToken);
                    emailService2.sendMessageUsingThymeleafTemplate(new String[]{user.getEmail()}, messageSource.getMessage("confirmation_email", null, Helper.getLocale(user)), variables, "signup.html", Helper.getLocale(user));
                }
                userRepository.save(user);

                return new SuccessResponse(true, "Successful registration. Check your mailbox to activate your account");
            }
        } else {
            throw new CustomException("Email is already in use", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    public void delete(String username) {
        userRepository.deleteByUsername(username);
    }

    public Optional<OwnUser> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<OwnUser> findByEmailAndCompany(String email, Long companyId) {
        return userRepository.findByEmailAndCompanyId(email, companyId);
    }

    public Optional<OwnUser> findByIdAndCompany(Long id, Long companyId) {
        return userRepository.findByIdAndCompanyId(id, companyId);
    }

    public OwnUser whoami(HttpServletRequest req) {
        return userRepository.findByEmail(jwtTokenProvider.getUsername(jwtTokenProvider.resolveToken(req))).get();
    }

    public String refresh(String username) {
        return jwtTokenProvider.createToken(username, Arrays.asList(userRepository.findByEmail(username).get().getRole().getRoleType()));
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

    public void enableUser(String email) {
        OwnUser user = userRepository.findByEmail(email).get();
        user.setEnabled(true);
        userRepository.save(user);
    }

    public SuccessResponse resetPassword(String email) {
        OwnUser user = findByEmail(email).get();
        Helper helper = new Helper();
        String password = helper.generateString().replace("-", "").substring(0, 8).toUpperCase();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("loginLink", frontendUrl + "/account/login?email=" + email);
            put("featuresLink", frontendUrl + "/#key-features");
            put("password", password);
        }};
        emailService2.sendMessageUsingThymeleafTemplate(new String[]{email}, messageSource.getMessage("password_reset", null, Helper.getLocale(user)), variables, "reset-password.html", Helper.getLocale(user));
        return new SuccessResponse(true, "Password changed successfully");
    }

    public Collection<OwnUser> findByCompany(Long id) {
        return userRepository.findByCompanyId(id);
    }

    public Collection<OwnUser> findByLocation(Long id) {
        return userRepository.findByLocation_Id(id);
    }

    public void invite(String email, Role role, OwnUser inviter) {
        if (!userRepository.existsByEmail(email) && Helper.isValidEmailAddress(email)) {
            userInvitationService.create(new UserInvitation(email, role));
            Map<String, Object> variables = new HashMap<String, Object>() {{
                put("joinLink", frontendUrl + "/account/register?" + "email=" + email + "&role=" + role.getId());
                put("featuresLink", frontendUrl + "/#key-features");
                put("inviter", inviter.getFirstName() + " " + inviter.getLastName());
                put("company", inviter.getCompany().getName());
            }};
            emailService2.sendMessageUsingThymeleafTemplate(new String[]{email}, messageSource.getMessage("invitation_to_use", null, Helper.getLocale(inviter)), variables, "invite.html", Helper.getLocale(inviter));
        } else throw new CustomException("Email already in use", HttpStatus.NOT_ACCEPTABLE);
    }

    @org.springframework.transaction.annotation.Transactional
    public OwnUser update(Long id, UserPatchDTO userReq) {
        if (userRepository.existsById(id)) {
            OwnUser savedUser = userRepository.findById(id).get();
            OwnUser updatedUser = userRepository.saveAndFlush(userMapper.updateUser(savedUser, userReq));
            em.refresh(updatedUser);
            return updatedUser;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public OwnUser save(OwnUser user) {
        return userRepository.save(user);
    }

    public Collection<OwnUser> saveAll(Collection<OwnUser> users) {
        return userRepository.saveAll(users);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserInCompany(OwnUser user, long companyId, boolean optional) {
        if (optional) {
            Optional<OwnUser> optionalUser = user == null ? Optional.empty() : findById(user.getId());
            return user == null || (optionalUser.isPresent() && optionalUser.get().getCompany().getId().equals(companyId));
        } else {
            Optional<OwnUser> optionalUser = findById(user.getId());
            return optionalUser.isPresent() && optionalUser.get().getCompany().getId().equals(companyId);
        }
    }


    public Page<OwnUser> findBySearchCriteria(SearchCriteria searchCriteria) {
        SpecificationBuilder<OwnUser> builder = new SpecificationBuilder<>();
        searchCriteria.getFilterFields().forEach(builder::with);
        Pageable page = PageRequest.of(searchCriteria.getPageNum(), searchCriteria.getPageSize(), searchCriteria.getDirection(), "id");
        return userRepository.findAll(builder.build(), page);
    }
}
