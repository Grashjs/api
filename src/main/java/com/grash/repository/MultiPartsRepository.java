package com.grash.repository;

import com.grash.model.MultiParts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface MultiPartsRepository extends JpaRepository<MultiParts, Long> {
    Collection<MultiParts> findByCompanyId(@Param("x") Long id);
}
