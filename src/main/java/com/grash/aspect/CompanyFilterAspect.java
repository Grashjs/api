package com.grash.aspect;

import com.grash.exception.CustomException;
import com.grash.model.OwnUser;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

@Aspect
@Component
@RequiredArgsConstructor
public class CompanyFilterAspect {

    private final EntityManager entityManager;

    @Before("execution(@com.grash.aspect.annotations.MultiTenantSearchMethod * *(..))")
    public void setTenantFilterForSearch() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof OwnUser) {
            OwnUser user = (OwnUser) authentication.getPrincipal();

            if (user.getCompany() != null) {
                Session session = entityManager.unwrap(Session.class);

                Filter filter = session.enableFilter("companyFilter");
                filter.setParameter("companyId", user.getCompany().getId().intValue());
            } else {
                throw new CustomException("The user (username=" + user.getUsername() + ")  has no Company", HttpStatus.NOT_ACCEPTABLE);
            }
        } else {
            Session session = entityManager.unwrap(Session.class);

            Filter filter = session.enableFilter("companyFilter");
            filter.setParameter("companyId", -1);
        }


    }


//    @Pointcut("execution (* org.hibernate.internal.SessionFactoryImpl.SessionBuilderImpl.openSession(..))")
//    public void openSession() {
//    }
//
//    @AfterReturning(pointcut = "openSession()", returning = "session")
//    public void afterOpenSession(Object session) {
//
//    	System.out.println("afterOpenSession");
//
//        if (session != null && Session.class.isInstance(session)) {
//    		User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    		AppClient appClient = user.getAppClient();
//
//            final Long tenantId = appClient != null ? appClient.getId() : null;
//            if (tenantId != null) {
//                org.hibernate.Filter filter = ((Session) session).enableFilter("tenantFilter");
//                filter.setParameter("tenantId", tenantId);
//                System.out.println("afterOpenSession set tenantId parameter to " + tenantId);
//            }
//        }
//    }


}
