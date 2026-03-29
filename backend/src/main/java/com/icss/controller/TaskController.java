package com.icss.controller;

import com.icss.dto.ApiResponse;
import com.icss.dto.FaultTaskRequest;
import com.icss.dto.GrabTaskRequest;
import com.icss.model.ServiceTask;
import com.icss.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ApiResponse<List<ServiceTask>> list(
            @RequestParam Integer level,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(taskService.getTaskList(level, status));
    }

    @PostMapping
    public ApiResponse<ServiceTask> create(@RequestBody ServiceTask task) {
        return ApiResponse.success(taskService.createTask(task));
    }

    @PutMapping("/{id}")
    public ApiResponse<ServiceTask> update(@PathVariable String id,
                                           @RequestBody ServiceTask task) {
        task.setId(id);
        task.setUpdatedAt(System.currentTimeMillis());
        return ApiResponse.success(taskService.createTask(task));
    }

    @PutMapping("/{id}/grab")
    public ApiResponse<ServiceTask> grab(@PathVariable String id,
                                         @RequestBody GrabTaskRequest req) {
        return ApiResponse.success(taskService.grabTask(id, req));
    }

    @PutMapping("/{id}/escalate")
    public ApiResponse<ServiceTask> escalate(@PathVariable String id) {
        return ApiResponse.success(taskService.escalateTask(id));
    }

    @PutMapping("/{id}/complete")
    public ApiResponse<ServiceTask> complete(@PathVariable String id) {
        return ApiResponse.success(taskService.completeTask(id));
    }

    @PostMapping("/fault")
    public ApiResponse<ServiceTask> createFault(@Valid @RequestBody FaultTaskRequest req) {
        return ApiResponse.success(taskService.createFaultTask(req));
    }
}
