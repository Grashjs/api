package com.grash.service;

import com.grash.dto.SubscriptionPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.SubscriptionMapper;
import com.grash.model.OwnUser;
import com.grash.model.Subscription;
import com.grash.model.enums.RoleType;
import com.grash.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final CompanyService companyService;
    private final SubscriptionMapper subscriptionMapper;
    private final EntityManager em;

    @Transactional
    public Subscription create(Subscription subscription) {
        Subscription savedSubscription = subscriptionRepository.saveAndFlush(subscription);
        em.refresh(savedSubscription);
        return savedSubscription;
    }

    @Transactional
    public Subscription update(Long id, SubscriptionPatchDTO subscriptionPatchDTO) {
        if (subscriptionRepository.existsById(id)) {
            Subscription savedSubscription = subscriptionRepository.findById(id).get();
            Subscription updatedSubscription = subscriptionRepository.save(subscriptionMapper.updateSubscription(savedSubscription, subscriptionPatchDTO));
            em.refresh(updatedSubscription);
            return updatedSubscription;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Subscription> getAll() {
        return subscriptionRepository.findAll();
    }

    public void delete(Long id) {
        subscriptionRepository.deleteById(id);
    }

    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }


    public boolean hasAccess(OwnUser user, Subscription subscription) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return true;
    }

    public boolean canCreate(OwnUser user, Subscription subscriptionReq) {
        Long companyId = user.getCompany().getId();
        return true;
    }

    public boolean canPatch(OwnUser user, SubscriptionPatchDTO subscriptionReq) {
        return true;
    }
}
