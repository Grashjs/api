package com.grash.service;

import com.grash.dto.WorkflowActionPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.WorkflowActionMapper;
import com.grash.model.OwnUser;
import com.grash.model.WorkflowAction;
import com.grash.model.enums.RoleType;
import com.grash.repository.WorkflowActionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkflowActionService {
    private final WorkflowActionRepository workflowActionRepository;
    private final WorkflowActionMapper workflowActionMapper;

    public WorkflowAction create(WorkflowAction WorkflowAction) {
        return workflowActionRepository.save(WorkflowAction);
    }

    public WorkflowAction update(Long id, WorkflowActionPatchDTO workflowActionsPatchDTO) {
        if (workflowActionRepository.existsById(id)) {
            WorkflowAction savedWorkflowAction = workflowActionRepository.findById(id).get();
            return workflowActionRepository.save(workflowActionMapper.updateWorkflowAction(savedWorkflowAction, workflowActionsPatchDTO));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<WorkflowAction> getAll() {
        return workflowActionRepository.findAll();
    }

    public void delete(Long id) {
        workflowActionRepository.deleteById(id);
    }

    public Optional<WorkflowAction> findById(Long id) {
        return workflowActionRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, WorkflowAction workflowAction) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(workflowAction.getCompanyId());
    }

    public boolean canPatch(OwnUser user, WorkflowActionPatchDTO workflowAction) {
        return true;
    }
}
