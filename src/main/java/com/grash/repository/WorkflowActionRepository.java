package com.grash.repository;

import com.grash.model.WorkflowAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkflowActionRepository extends JpaRepository<WorkflowAction, Long> {
    Collection<WorkflowAction> findByCompanyId(Long id);
}
