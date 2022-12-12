package com.grash.service;

import com.grash.dto.TaskPatchDTO;
import com.grash.exception.CustomException;
import com.grash.mapper.TaskMapper;
import com.grash.model.Company;
import com.grash.model.OwnUser;
import com.grash.model.Task;
import com.grash.model.WorkOrder;
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
            Task updatedTask = taskRepository.save(taskMapper.updateTask(savedTask, task));
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
        } else return user.getCompany().getId().equals(task.getCompany().getId());
    }

    public boolean canCreate(OwnUser user, Task taskReq) {
        Long companyId = user.getCompany().getId();

        Optional<Company> optionalCompany = companyService.findById(taskReq.getCompany().getId());
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(taskReq.getWorkOrder().getId());

        //@NotNull fields
        boolean first = optionalCompany.isPresent() && optionalCompany.get().getId().equals(companyId);
        boolean second = optionalWorkOrder.isPresent() && optionalWorkOrder.get().getCompany().getId().equals(companyId);

        return first && second && canPatch(user, taskMapper.toDto(taskReq));
    }

    public boolean canPatch(OwnUser user, TaskPatchDTO taskReq) {
        return true;
    }

    public Collection<Task> findByWorkOrder(Long id) {
        return taskRepository.findByWorkOrder_Id(id);
    }
}
