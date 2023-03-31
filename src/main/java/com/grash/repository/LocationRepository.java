package com.grash.repository;

import com.grash.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long>, JpaSpecificationExecutor<Location> {
    Collection<Location> findByCompanyId(Long id);

    Collection<Location> findByParentLocation_Id(Long id);

    Optional<Location> findByNameAndCompanyId(String locationName, Long companyId);

    Optional<Location> findByIdAndCompanyId(Long id, Long companyId);
}
