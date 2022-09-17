package com.grash.repository;

import com.grash.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
    @Query("SELECT r from Role r where r.companySettings.company = :x ")
    Collection<Role> findByCompany_Id(Long id);
}
