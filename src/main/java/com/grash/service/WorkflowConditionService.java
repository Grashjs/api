package com.grash.service;

import com.grash.dto.WorkflowConditionPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.WorkflowConditionMapper;
import com.grash.model.OwnUser;
import com.grash.model.WorkflowCondition;
import com.grash.model.enums.RoleType;
import com.grash.repository.WorkflowConditionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowConditionService {
    private final WorkflowConditionRepository workflowConditionRepository;
    private final WorkflowConditionMapper workflowConditionMapper;

    public WorkflowCondition create(WorkflowCondition WorkflowCondition) {
        return workflowConditionRepository.save(WorkflowCondition);
    }

    public WorkflowCondition update(Long id, WorkflowConditionPatchDTO workflowConditionsPatchDTO) {
        if (workflowConditionRepository.existsById(id)) {
            WorkflowCondition savedWorkflowCondition = workflowConditionRepository.findById(id).get();
            return workflowConditionRepository.save(workflowConditionMapper.updateWorkflowCondition(savedWorkflowCondition, workflowConditionsPatchDTO));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<WorkflowCondition> getAll() {
        return workflowConditionRepository.findAll();
    }

    public void delete(Long id) {
        workflowConditionRepository.deleteById(id);
    }

    public Optional<WorkflowCondition> findById(Long id) {
        return workflowConditionRepository.findById(id);
    }

    public Collection<WorkflowCondition> saveAll(Collection<WorkflowCondition> workflowConditions) {
        return workflowConditionRepository.saveAll(workflowConditions);
    }

    public boolean hasAccess(OwnUser user, WorkflowCondition workflowCondition) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workflowCondition.getCompanyId());
    }

    public boolean canPatch(OwnUser user, WorkflowConditionPatchDTO workflowCondition) {
        return true;
    }
}
