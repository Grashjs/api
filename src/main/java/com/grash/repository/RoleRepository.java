package com.grash.repository;

import com.grash.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT r from Role r where r.companySettings.company.id = :x ")
    Collection<Role> findByCompanyId(@Param("x") Long id);
}
