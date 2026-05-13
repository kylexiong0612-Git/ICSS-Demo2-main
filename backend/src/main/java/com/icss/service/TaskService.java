package com.icss.service;

import com.icss.dto.HandoffRequest;
import com.icss.dto.FaultTaskRequest;
import com.icss.dto.GrabTaskRequest;
import com.icss.dto.TaskAdvanceResponse;
import com.icss.mapper.TaskMapper;
import com.icss.model.AgentWorkflowRouteRule;
import com.icss.model.AgentWorkflowStage;
import com.icss.model.HandoffAnalysis;
import com.icss.model.ServiceTask;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskMapper taskMapper;
    private final WorkflowConfigService workflowConfigService;
    private final HandoffAnalysisService handoffAnalysisService;

    public List<ServiceTask> getTaskList(Integer level, String workflowCode, String stageCode, String customerId, String status) {
        return taskMapper.selectTasks(level, workflowCode, stageCode, customerId, status);
    }

    public ServiceTask getById(String id) {
        return taskMapper.selectById(id);
    }

    public ServiceTask createTask(ServiceTask task) {
        prepareNewTask(task);
        taskMapper.insert(task);
        return taskMapper.selectById(task.getId());
    }

    public ServiceTask updateTask(ServiceTask task) {
        ServiceTask existing = taskMapper.selectById(task.getId());
        if (existing == null) {
            return createTask(task);
        }
        task.setCreatedAt(existing.getCreatedAt());
        if (task.getWorkflowCode() == null || task.getWorkflowCode().isBlank()) {
            task.setWorkflowCode(existing.getWorkflowCode());
        }
        if (task.getCurrentStageCode() == null || task.getCurrentStageCode().isBlank()) {
            task.setCurrentStageCode(existing.getCurrentStageCode());
            task.setCurrentStageOrder(existing.getCurrentStageOrder());
        }
        if (task.getLevel() == null) task.setLevel(existing.getLevel());
        task.setUpdatedAt(System.currentTimeMillis());
        taskMapper.update(task);
        return taskMapper.selectById(task.getId());
    }

    public HandoffAnalysis analyzeHandoff(HandoffRequest request) {
        return handoffAnalysisService.analyzeAndRoute(request);
    }

    public ServiceTask createHandoffTask(HandoffRequest request) {
        HandoffAnalysis analysis = analyzeHandoff(request);
        AgentWorkflowRouteRule routeRule = workflowConfigService.getRouteRule(analysis.getIntentCode());
        AgentWorkflowStage entryStage = workflowConfigService.resolveStage(
                analysis.getTargetWorkflowCode(),
                analysis.getTargetStageCode()
        );

        ServiceTask task = new ServiceTask();
        task.setId("TASK-" + System.currentTimeMillis());
        task.setCustomerId(request.getCustomerId());
        task.setRequestSource("Customer");
        task.setStatus("Pending");
        task.setPriority(resolvePriority(routeRule != null ? routeRule.getPriorityStrategy() : null, analysis.getIntentCode()));
        task.setCategory(analysis.getIntentName());
        task.setSummary(analysis.getSummary());
        task.setAiSuggestion(analysis.getSuggestion());
        task.setTags(analysis.getTags());
        task.setWorkflowCode(entryStage != null ? entryStage.getWorkflowCode() : "ops-service-flow");
        task.setCurrentStageCode(entryStage != null ? entryStage.getCode() : "L1");
        task.setCurrentStageOrder(entryStage != null ? entryStage.getStageOrder() : 1);
        task.setLevel(entryStage != null ? entryStage.getStageOrder() : 1);
        task.setIntentCode(analysis.getIntentCode());
        task.setRouteReason(analysis.getRouteReason());
        task.setHandoffConfidence(analysis.getConfidence());
        task.setUnreadCount(0);
        prepareNewTask(task);
        taskMapper.insert(task);
        return taskMapper.selectById(task.getId());
    }

    public TaskAdvanceResponse advanceTask(String taskId) {
        ServiceTask currentTask = taskMapper.selectById(taskId);
        if (currentTask == null) {
            return null;
        }

        AgentWorkflowStage nextStage = workflowConfigService.getNextStage(
                currentTask.getWorkflowCode(),
                currentTask.getCurrentStageOrder()
        );

        if (nextStage == null) {
            currentTask = updateTaskStatus(currentTask, "Completed");
            TaskAdvanceResponse response = new TaskAdvanceResponse();
            response.setCurrentTask(currentTask);
            response.setNextTask(null);
            return response;
        }

        currentTask.setStatus("Escalated");
        currentTask.setAssignedTo(null);
        currentTask.setUpdatedAt(System.currentTimeMillis());
        taskMapper.update(currentTask);

        ServiceTask nextTask = new ServiceTask();
        nextTask.setId("TASK-" + System.currentTimeMillis());
        nextTask.setCustomerId(currentTask.getCustomerId());
        nextTask.setAgentId(currentTask.getAgentId());
        nextTask.setRequestSource("Agent");
        nextTask.setStatus("Pending");
        nextTask.setPriority(currentTask.getPriority());
        nextTask.setCategory(currentTask.getCategory());
        nextTask.setSummary(currentTask.getSummary());
        nextTask.setAiSuggestion(currentTask.getAiSuggestion());
        nextTask.setUnreadCount(currentTask.getUnreadCount());
        nextTask.setTags(currentTask.getTags());
        nextTask.setWorkflowCode(nextStage.getWorkflowCode());
        nextTask.setCurrentStageCode(nextStage.getCode());
        nextTask.setCurrentStageOrder(nextStage.getStageOrder());
        nextTask.setLevel(nextStage.getStageOrder());
        nextTask.setSourceTaskId(currentTask.getId());
        nextTask.setIntentCode(currentTask.getIntentCode());
        nextTask.setRouteReason("advance:" + currentTask.getCurrentStageCode() + "->" + nextStage.getCode());
        nextTask.setHandoffConfidence(currentTask.getHandoffConfidence());
        prepareNewTask(nextTask);
        taskMapper.insert(nextTask);

        TaskAdvanceResponse response = new TaskAdvanceResponse();
        response.setCurrentTask(taskMapper.selectById(currentTask.getId()));
        response.setNextTask(taskMapper.selectById(nextTask.getId()));
        return response;
    }

    public ServiceTask createFaultTask(FaultTaskRequest req) {
        AgentWorkflowStage entryStage = workflowConfigService.resolveStage("system-fault-flow", "L1");
        ServiceTask task = new ServiceTask();
        task.setId("FAULT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        task.setRequestSource("Agent");
        task.setStatus("Pending");
        task.setPriority(req.getUrgency());
        task.setCategory(req.getFaultType() != null ? req.getFaultType() : "系统报障");
        task.setSummary(req.getDescription());
        task.setWorkflowCode(entryStage != null ? entryStage.getWorkflowCode() : "system-fault-flow");
        task.setCurrentStageCode(entryStage != null ? entryStage.getCode() : "L1");
        task.setCurrentStageOrder(entryStage != null ? entryStage.getStageOrder() : 1);
        task.setLevel(entryStage != null ? entryStage.getStageOrder() : 1);
        task.setIntentCode("system-fault");
        task.setRouteReason("agent:fault-report");
        task.setUnreadCount(0);
        task.setTags(List.of("系统报障", req.getFaultType() != null ? req.getFaultType() : "故障工单"));
        prepareNewTask(task);
        taskMapper.insert(task);
        return taskMapper.selectById(task.getId());
    }

    public ServiceTask grabTask(String taskId, GrabTaskRequest req) {
        taskMapper.grab(taskId, req.getAgentId(), System.currentTimeMillis());
        return taskMapper.selectById(taskId);
    }

    public ServiceTask completeTask(String taskId) {
        ServiceTask task = taskMapper.selectById(taskId);
        if (task == null) return null;
        return updateTaskStatus(task, "Completed");
    }

    private ServiceTask updateTaskStatus(ServiceTask task, String status) {
        taskMapper.updateStatus(task.getId(), status, System.currentTimeMillis());
        return taskMapper.selectById(task.getId());
    }

    private void prepareNewTask(ServiceTask task) {
        if (task.getId() == null || task.getId().isBlank()) {
            task.setId("TASK-" + System.currentTimeMillis());
        }
        long now = System.currentTimeMillis();
        if (task.getCreatedAt() == null) task.setCreatedAt(now);
        task.setUpdatedAt(now);
        if (task.getStatus() == null) task.setStatus("Pending");
        if (task.getPriority() == null) task.setPriority("Medium");
        if (task.getRequestSource() == null) task.setRequestSource("Customer");
        if (task.getCategory() == null) task.setCategory("通用咨询");
        if (task.getUnreadCount() == null) task.setUnreadCount(0);
        assignWorkflowStage(task);
    }

    private void assignWorkflowStage(ServiceTask task) {
        AgentWorkflowStage stage;
        if (task.getWorkflowCode() != null && !task.getWorkflowCode().isBlank()) {
            stage = workflowConfigService.resolveStage(task.getWorkflowCode(), task.getCurrentStageCode());
        } else {
            stage = workflowConfigService.resolveStageByLevel(task.getLevel());
        }
        if (stage != null) {
            task.setWorkflowCode(stage.getWorkflowCode());
            task.setCurrentStageCode(stage.getCode());
            task.setCurrentStageOrder(stage.getStageOrder());
            if (task.getLevel() == null) {
                task.setLevel(stage.getStageOrder());
            }
        } else if (task.getLevel() == null) {
            task.setLevel(1);
            task.setWorkflowCode("ops-service-flow");
            task.setCurrentStageCode("L1");
            task.setCurrentStageOrder(1);
        }
    }

    private String resolvePriority(String strategy, String intentCode) {
        if ("force-urgent".equals(strategy)) return "Urgent";
        if ("force-high".equals(strategy)) return "High";
        if ("force-medium".equals(strategy)) return "Medium";
        if ("system-fault".equals(intentCode)) return "Urgent";
        if ("complaint-service".equals(intentCode)) return "High";
        return "Medium";
    }
}
