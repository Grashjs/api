package com.grash.repository;

import com.grash.model.OwnUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

public interface UserRepository extends JpaRepository<OwnUser, Long> {

    boolean existsByUsername(String username);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    OwnUser findUserByEmail(String email);

    @Transactional
    void deleteByUsername(String username);

    boolean existsByEmail(String email);

    Collection<OwnUser> findByCompany_Id(Long id);

    Collection<OwnUser> findByLocation_Id(Long id);
}
