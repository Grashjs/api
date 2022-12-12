package com.grash.repository;

import com.grash.model.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    Collection<Meter> findByCompany_Id(Long id);

    Collection<Meter> findByAsset_Id(Long id);
}
