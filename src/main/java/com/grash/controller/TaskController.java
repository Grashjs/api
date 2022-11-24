package com.grash.controller;

import com.grash.dto.SuccessResponse;
import com.grash.dto.TaskBaseDTO;
import com.grash.dto.TaskPatchDTO;
import com.grash.exception.CustomException;
import com.grash.model.*;
import com.grash.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks")
@Api(tags = "task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final TaskOptionService taskOptionService;
    private final TaskBaseService taskBaseService;
    private final WorkOrderService workOrderService;

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Task not found")})
    public Task getById(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Task> optionalTask = taskService.findById(id);
        if (optionalTask.isPresent()) {
            Task savedTask = optionalTask.get();
            if (taskService.hasAccess(user, savedTask)) {
                return savedTask;
            } else throw new CustomException("Access denied", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/work-order/{id}")
    @PreAuthorize("permitAll()")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"),
            @ApiResponse(code = 403, message = "Access denied"),
            @ApiResponse(code = 404, message = "Task not found")})
    public Collection<Task> getByWorkOrder(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(id);
        if (optionalWorkOrder.isPresent() && workOrderService.hasAccess(user, optionalWorkOrder.get())) {
            return taskService.findByWorkOrder(id);
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping("/work-order/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied")})
    public Collection<Task> create(@ApiParam("Task") @Valid @RequestBody Collection<TaskBaseDTO> taskBasesReq, @ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<WorkOrder> optionalWorkOrder = workOrderService.findById(id);
        if (optionalWorkOrder.isPresent() && workOrderService.hasAccess(user, optionalWorkOrder.get())) {
            Collection<TaskBase> taskBases = taskBasesReq.stream().map(taskBaseDTO -> {
                TaskBase taskBase = TaskBase.builder()
                        .label(taskBaseDTO.getLabel())
                        .taskType(taskBaseDTO.getTaskType())
                        .user(taskBaseDTO.getUser())
                        .asset(taskBaseDTO.getAsset())
                        .meter(taskBaseDTO.getMeter())
                        .build();
                taskBase.setCompany(user.getCompany());
                TaskBase savedTaskBase = taskBaseService.create(taskBase);

                if (taskBaseDTO.getOptions() != null) {
                    taskBaseDTO.getOptions().forEach(option -> {
                        TaskOption taskOption = new TaskOption(option, user.getCompany(), savedTaskBase);
                        taskOptionService.create(taskOption);
                    });
                }
                return savedTaskBase;

            }).collect(Collectors.toList());
            return taskBases.stream().map(taskBase -> {
                Task task = new Task(taskBase, optionalWorkOrder.get(), user.getCompany());
                return taskService.create(task);
            }).collect(Collectors.toList());
        } else throw new CustomException("Not found", HttpStatus.NOT_FOUND);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Task not found")})
    public Task patch(@ApiParam("Task") @Valid @RequestBody TaskPatchDTO task, @ApiParam("id") @PathVariable("id") Long id,
                      HttpServletRequest req) {
        OwnUser user = userService.whoami(req);
        Optional<Task> optionalTask = taskService.findById(id);

        if (optionalTask.isPresent()) {
            Task savedTask = optionalTask.get();
            if (taskService.hasAccess(user, savedTask) && taskService.canPatch(user, task)) {
                return taskService.update(id, task);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Task not found", HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_CLIENT')")
    @ApiResponses(value = {//
            @ApiResponse(code = 500, message = "Something went wrong"), //
            @ApiResponse(code = 403, message = "Access denied"), //
            @ApiResponse(code = 404, message = "Task not found")})
    public ResponseEntity delete(@ApiParam("id") @PathVariable("id") Long id, HttpServletRequest req) {
        OwnUser user = userService.whoami(req);

        Optional<Task> optionalTask = taskService.findById(id);
        if (optionalTask.isPresent()) {
            Task savedTask = optionalTask.get();
            if (taskService.hasAccess(user, savedTask)) {
                taskService.delete(id);
                return new ResponseEntity(new SuccessResponse(true, "Deleted successfully"),
                        HttpStatus.OK);
            } else throw new CustomException("Forbidden", HttpStatus.FORBIDDEN);
        } else throw new CustomException("Task not found", HttpStatus.NOT_FOUND);
    }

}
