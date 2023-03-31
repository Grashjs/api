package com.grash.repository;

import com.grash.model.Reading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ReadingRepository extends JpaRepository<Reading, Long> {
    @Query("SELECT r from Reading r where r.meter.companyId.id = :x ")
    Collection<Reading> findByCompanyId(@Param("x") Long id);

    Collection<Reading> findByMeter_Id(Long id);
}
