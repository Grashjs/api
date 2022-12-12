package com.grash.repository;

import com.grash.model.WorkOrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkOrderHistoryRepository extends JpaRepository<WorkOrderHistory, Long> {
    Collection<WorkOrderHistory> findByWorkOrder_Id(Long id);
}
