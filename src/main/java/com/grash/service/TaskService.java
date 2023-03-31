package com.grash.service;

import com.grash.dto.TaskPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.TaskMapper;
import com.grash.model.OwnUser;
import com.grash.model.Task;
import com.grash.model.enums.RoleType;
import com.grash.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final WorkOrderService workOrderService;
    private final CompanyService companyService;
    private final FileService fileService;
    private final TaskMapper taskMapper;
    private final EntityManager em;

    @Transactional
    public Task create(Task Task) {
        Task savedTask = taskRepository.saveAndFlush(Task);
        em.refresh(savedTask);
        return savedTask;
    }

    @Transactional
    public Task update(Long id, TaskPatchDTO task) {
        if (taskRepository.existsById(id)) {
            Task savedTask = taskRepository.findById(id).get();
            Task updatedTask = taskRepository.saveAndFlush(taskMapper.updateTask(savedTask, task));
            em.refresh(updatedTask);
            return updatedTask;
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    public Collection<Task> getAll() {
        return taskRepository.findAll();
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public boolean hasAccess(OwnUser user, Task task) {
        if (user.getRole().getRoleType().equals(RoleType.ROLE_SUPER_ADMIN)) {
            return true;
        } else return user.getCompany().getId().equals(task.getCompanyId());
    }

    public boolean canCreate(OwnUser user, Task taskReq) {
        Long companyId = user.getCompany().getId();
        //@NotNull fields
        boolean first = taskReq.getCompanyId().equals(companyId);
        boolean second = workOrderService.isWorkOrderInCompany(taskReq.getWorkOrder(), companyId, false);
        boolean third = taskReq.getImages() == null || taskReq.getImages().stream().allMatch(item ->
                fileService.isFileInCompany(item, companyId, false));
        return first && second && third && canPatch(user, taskMapper.toPatchDto(taskReq));
    }

    public boolean canPatch(OwnUser user, TaskPatchDTO taskReq) {
        return true;
    }

    public Collection<Task> findByWorkOrder(Long id) {
        return taskRepository.findByWorkOrder_Id(id);
    }
}
