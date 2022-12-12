package com.grash.repository;

import com.grash.model.AdditionalTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface AdditionalTimeRepository extends JpaRepository<AdditionalTime, Long> {
    Collection<AdditionalTime> findByWorkOrder_Id(Long id);
}
