package com.grash;

import com.grash.model.Company;
import com.grash.model.Role;
import com.grash.model.SubscriptionPlan;
import com.grash.model.enums.PlanFeatures;
import com.grash.model.enums.RoleCode;
import com.grash.model.enums.RoleType;
import com.grash.service.CompanyService;
import com.grash.service.RoleService;
import com.grash.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.HashSet;

@SpringBootApplication
@RequiredArgsConstructor
public class ApiApplication implements CommandLineRunner {

    @Value("${superAdmin.role.name}")
    private String superAdminRole;

    private final RoleService roleService;
    private final CompanyService companyService;
    private final SubscriptionPlanService subscriptionPlanService;

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (!roleService.findByName(superAdminRole).isPresent()) {
            Company company = companyService.create(new Company());
            roleService.create(Role.builder().
                    name(superAdminRole)
                    .companySettings(company.getCompanySettings())
                    .code(RoleCode.ADMIN)
                    .roleType(RoleType.ROLE_SUPER_ADMIN)
                    .build());
        }
        if (!subscriptionPlanService.existByCode("FREE")) {
            subscriptionPlanService.create(SubscriptionPlan.builder()
                    .code("FREE")
                    .name("Free")
                    .monthlyCostPerUser(0)
                    .yearlyCostPerUser(0).build());
        }
        if (!subscriptionPlanService.existByCode("STARTER")) {
            subscriptionPlanService.create(SubscriptionPlan.builder()
                    .code("STARTER")
                    .name("Starter").features(new HashSet<>(Arrays.asList(PlanFeatures.PREVENTIVE_MAINTENANCE,
                            PlanFeatures.CHECKLIST,
                            PlanFeatures.FILE)))
                    .monthlyCostPerUser(20)
                    .yearlyCostPerUser(200).build());
        }
        if (!subscriptionPlanService.existByCode("PRO")) {
            subscriptionPlanService.create(SubscriptionPlan.builder()
                    .code("PRO")
                    .name("Professional")
                    .monthlyCostPerUser(40)
                    .features(new HashSet<>(Arrays.asList(PlanFeatures.PREVENTIVE_MAINTENANCE,
                            PlanFeatures.CHECKLIST,
                            PlanFeatures.FILE,
                            PlanFeatures.REQUEST_CONFIGURATION,
                            PlanFeatures.ADDITIONAL_TIME
                    )))
                    .yearlyCostPerUser(400).build());
        }
        if (!subscriptionPlanService.existByCode("BUS")) {
            subscriptionPlanService.create(SubscriptionPlan.builder()
                    .code("BUS")
                    .name("Business")
                    .monthlyCostPerUser(80)
                    .features(new HashSet<>(Arrays.asList(PlanFeatures.values())))
                    .yearlyCostPerUser(800).build());
        }
    }
}
