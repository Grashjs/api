package com.grash.repository;

import com.grash.model.OwnUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository extends JpaRepository<OwnUser, Long>, JpaSpecificationExecutor<OwnUser> {

    boolean existsByUsername(String username);

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    Optional<OwnUser> findByEmail(String email);

    @Transactional
    void deleteByUsername(String username);

    boolean existsByEmail(String email);

    Collection<OwnUser> findByCompanyId(Long id);

    Collection<OwnUser> findByLocation_Id(Long id);

    Optional<OwnUser> findByEmailAndCompanyId(String email, Long companyId);

    Optional<OwnUser> findByIdAndCompanyId(Long id, Long companyId);
}
