package com.grash.repository;

import com.grash.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long>, JpaSpecificationExecutor<Meter> {
    Collection<Meter> findByCompanyId(Long id);

    Collection<Meter> findByAsset_Id(Long id);

    Optional<Meter> findByIdAndCompanyId(Long id, Long companyId);
}
