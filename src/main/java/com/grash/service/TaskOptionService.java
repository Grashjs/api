package com.grash.service;

import com.grash.dto.TaskOptionPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.TaskOptionMapper;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.TaskOption;
import com.grash.model.enums.RoleType;
import com.grash.repository.TaskOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskOptionService {
    private final TaskOptionRepository taskOptionRepository;
    private final CompanyService companyService;
    private final TaskOptionMapper taskOptionMapper;

    public TaskOption create(TaskOption TaskOption) {
        return taskOptionRepository.save(TaskOption);
    }

    public TaskOption update(Long id, TaskOptionPatchDTO taskOption) {
        if (taskOptionRepository.existsById(id)) {
            TaskOption savedTaskOption = taskOptionRepository.findById(id).get();
            return taskOptionRepository.save(taskOptionMapper.updateTaskOption(savedTaskOption, taskOption));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<TaskOption> getAll() {
        return taskOptionRepository.findAll();
    }

    public void delete(Long id) {
        taskOptionRepository.deleteById(id);
    }

    public Optional<TaskOption> findById(Long id) {
        return taskOptionRepository.findById(id);
    }


    public boolean hasAccess(OwnUser user, TaskOption taskOption) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(taskOption.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, TaskOption taskOptionReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(taskOptionReq.getCompany().getId());

        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);

        return first;
    }

}
