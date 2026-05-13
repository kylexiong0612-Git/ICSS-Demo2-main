package com.icss.controller;

import com.icss.dto.ApiResponse;
import com.icss.model.AgentWorkflowRouteRule;
import com.icss.model.AgentWorkflowTemplate;
import com.icss.model.WorkbenchLayoutConfig;
import com.icss.service.WorkflowConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkflowConfigController {

    private final WorkflowConfigService workflowConfigService;

    @GetMapping("/agent-workflows")
    public ApiResponse<List<AgentWorkflowTemplate>> listWorkflows() {
        return ApiResponse.success(workflowConfigService.getAllTemplates());
    }

    @GetMapping("/agent-workflows/{code}")
    public ApiResponse<AgentWorkflowTemplate> getWorkflow(@PathVariable String code) {
        return ApiResponse.success(workflowConfigService.getTemplate(code));
    }

    @PostMapping("/agent-workflows")
    public ApiResponse<AgentWorkflowTemplate> createWorkflow(@RequestBody AgentWorkflowTemplate template) {
        return ApiResponse.success(workflowConfigService.saveTemplate(template));
    }

    @PutMapping("/agent-workflows/{code}")
    public ApiResponse<AgentWorkflowTemplate> updateWorkflow(@PathVariable String code,
                                                             @RequestBody AgentWorkflowTemplate template) {
        template.setCode(code);
        return ApiResponse.success(workflowConfigService.saveTemplate(template));
    }

    @GetMapping("/agent-workflow-routes")
    public ApiResponse<List<AgentWorkflowRouteRule>> listRouteRules() {
        return ApiResponse.success(workflowConfigService.getAllRouteRules());
    }

    @PutMapping("/agent-workflow-routes/{intentCode}")
    public ApiResponse<AgentWorkflowRouteRule> updateRouteRule(@PathVariable String intentCode,
                                                               @RequestBody AgentWorkflowRouteRule rule) {
        rule.setIntentCode(intentCode);
        return ApiResponse.success(workflowConfigService.saveRouteRule(rule));
    }

    @GetMapping("/workbench-layouts")
    public ApiResponse<List<WorkbenchLayoutConfig>> listLayouts() {
        return ApiResponse.success(workflowConfigService.getAllLayouts());
    }

    @GetMapping("/workbench-layouts/effective")
    public ApiResponse<WorkbenchLayoutConfig> getEffectiveLayout(@RequestParam String workflowCode,
                                                                 @RequestParam String stageCode) {
        return ApiResponse.success(workflowConfigService.getEffectiveLayout(workflowCode, stageCode));
    }

    @PostMapping("/workbench-layouts")
    public ApiResponse<WorkbenchLayoutConfig> createLayout(@RequestBody WorkbenchLayoutConfig layout) {
        return ApiResponse.success(workflowConfigService.saveLayout(layout));
    }

    @PutMapping("/workbench-layouts/{code}")
    public ApiResponse<WorkbenchLayoutConfig> updateLayout(@PathVariable String code,
                                                           @RequestBody WorkbenchLayoutConfig layout) {
        layout.setCode(code);
        return ApiResponse.success(workflowConfigService.saveLayout(layout));
    }
}
