package com.grash.repository;

import com.grash.model.Workflow;
import com.grash.model.enums.workflow.WFMainCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WorkflowRepository extends JpaRepository<Workflow, Long> {
    Collection<Workflow> findByCompanyId(Long id);

    Collection<Workflow> findByMainConditionAndCompanyId(WFMainCondition mainCondition, Long companyId);
}
