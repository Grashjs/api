package com.grash.model.abstracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.grash.exception.CustomException;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.enums.RoleType;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;

@MappedSuperclass
@Data
public class CompanyAudit extends Audit {
    @Column(name = "company_id", updatable = false)
    @JsonIgnore
    @NotNull
    private Long companyId;

    @PrePersist
    public void beforePersist() {
        OwnUser user = (OwnUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        this.setCompanyId(user.getCompany().getId());
    }

    @PostLoad
    public void afterLoad() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null) // && this instanceof ParameterUser)
            return;

        Object principal = authentication.getPrincipal();

        if (principal instanceof OwnUser) {
            OwnUser user = (OwnUser) principal;
            if (!user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
                Company company = user.getCompany();
                // check if not authorized
                if (!company.getId().equals(this.getCompanyId())) {
                    throw new CustomException("afterLoad:  the user (username=" + user.getUsername() + ")  is not authorized to load  this object (" + this.getClass(), HttpStatus.NOT_ACCEPTABLE);
                }
            }
        }
    }
}
