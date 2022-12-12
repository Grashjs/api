package com.grash.repository;

import com.grash.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    Collection<WorkOrder> findByCompany_Id(Long id);

    Collection<WorkOrder> findByAsset_Id(Long id);

    Collection<WorkOrder> findByLocation_Id(Long id);
}
