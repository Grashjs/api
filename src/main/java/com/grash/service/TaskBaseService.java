package com.grash.service;

import com.grash.dto.TaskBaseDTO;
import com.grash.dto.TaskBasePatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.TaskBaseMapper;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.TaskBase;
import com.grash.model.TaskOption;
import com.grash.model.enums.RoleType;
import com.grash.repository.TaskBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskBaseService {
    private final TaskBaseRepository taskBaseRepository;
    private final CompanyService companyService;
    private final TaskBaseMapper taskBaseMapper;
    private final TaskOptionService taskOptionService;
    private final EntityManager em;

    public TaskBase create(TaskBase TaskBase) {
        return taskBaseRepository.save(TaskBase);
    }

    @Transactional
    public TaskBase createFromTaskBaseDTO(TaskBaseDTO taskBaseDTO, Company company) {
        TaskBase taskBase = TaskBase.builder()
                .label(taskBaseDTO.getLabel())
                .taskType(taskBaseDTO.getTaskType())
                .user(taskBaseDTO.getUser())
                .asset(taskBaseDTO.getAsset())
                .meter(taskBaseDTO.getMeter())
                .build();
        taskBase.setCompany(company);
        TaskBase savedTaskBase = create(taskBase);

        if (taskBaseDTO.getOptions() != null) {
            taskBaseDTO.getOptions().forEach(option -> {
                TaskOption taskOption = new TaskOption(option, company, savedTaskBase);
                taskOptionService.create(taskOption);
            });
        }
        em.refresh(savedTaskBase);
        return savedTaskBase;
    }

    public TaskBase update(Long id, TaskBasePatchDTO taskBase) {
        if (taskBaseRepository.existsById(id)) {
            TaskBase savedTaskBase = taskBaseRepository.findById(id).get();
            return taskBaseRepository.save(taskBaseMapper.updateTaskBase(savedTaskBase, taskBase));
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<TaskBase> getAll() {
        return taskBaseRepository.findAll();
    }

    public void delete(Long id) {
        taskBaseRepository.deleteById(id);
    }

    public Optional<TaskBase> findById(Long id) {
        return taskBaseRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, TaskBase taskBase) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(taskBase.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, TaskBase taskBaseReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(taskBaseReq.getCompany().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);

        return first && canPatch(user, taskBaseMapper.toDto(taskBaseReq));
    }

    public boolean canPatch(OwnUser user, TaskBasePatchDTO taskBaseReq) {
        return true;
    }
}
