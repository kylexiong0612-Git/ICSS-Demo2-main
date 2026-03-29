package com.icss.service;

import com.icss.dto.FaultTaskRequest;
import com.icss.dto.GrabTaskRequest;
import com.icss.mapper.TaskMapper;
import com.icss.model.ServiceTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;

    public List<ServiceTask> getTaskList(Integer level, String status) {
        return taskMapper.selectByLevelAndStatus(level, status);
    }

    public ServiceTask getById(String id) {
        return taskMapper.selectById(id);
    }

    public ServiceTask createTask(ServiceTask task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId("TASK-" + System.currentTimeMillis());
        }
        long now = System.currentTimeMillis();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (task.getStatus() == null) task.setStatus("Pending");
        if (task.getPriority() == null) task.setPriority("Medium");
        if (task.getLevel() == null) task.setLevel(1);
        if (task.getRequestSource() == null) task.setRequestSource("Customer");
        if (task.getCategory() == null) task.setCategory("通用咨询");
        if (task.getUnreadCount() == null) task.setUnreadCount(0);
        taskMapper.insert(task);
        return task;
    }

    public ServiceTask grabTask(String taskId, GrabTaskRequest req) {
        taskMapper.grab(taskId, req.getAgentId(), System.currentTimeMillis());
        return taskMapper.selectById(taskId);
    }

    public ServiceTask escalateTask(String taskId) {
        taskMapper.escalate(taskId, System.currentTimeMillis());
        return taskMapper.selectById(taskId);
    }

    public ServiceTask completeTask(String taskId) {
        taskMapper.updateStatus(taskId, "Completed", System.currentTimeMillis());
        return taskMapper.selectById(taskId);
    }

    public ServiceTask createFaultTask(FaultTaskRequest req) {
        ServiceTask task = new ServiceTask();
        task.setId("FAULT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        task.setRequestSource("Agent");
        task.setStatus("Pending");
        task.setPriority(req.getUrgency());
        task.setCategory(req.getFaultType() != null ? req.getFaultType() : "故障工单");
        task.setSummary(req.getDescription());
        task.setLevel(2);
        task.setUnreadCount(0);
        long now = System.currentTimeMillis();
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        taskMapper.insert(task);
        return task;
    }
}
