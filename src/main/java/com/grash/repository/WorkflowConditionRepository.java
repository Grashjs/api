package com.grash.repository;

import com.grash.model.WorkflowCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkflowConditionRepository extends JpaRepository<WorkflowCondition, Long> {
    Collection<WorkflowCondition> findByCompanyId(Long id);
}
