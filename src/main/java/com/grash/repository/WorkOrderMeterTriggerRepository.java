package com.grash.repository;

import com.grash.model.WorkOrderMeterTrigger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkOrderMeterTriggerRepository extends JpaRepository<WorkOrderMeterTrigger, Long> {
    Collection<WorkOrderMeterTrigger> findByMeter_Id(Long id);
}
