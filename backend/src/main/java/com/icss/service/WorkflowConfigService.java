package com.icss.service;

import com.icss.mapper.WorkflowConfigMapper;
import com.icss.model.AgentWorkflowRouteRule;
import com.icss.model.AgentWorkflowStage;
import com.icss.model.AgentWorkflowTemplate;
import com.icss.model.WorkbenchLayoutConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowConfigService {

    private final WorkflowConfigMapper workflowConfigMapper;

    public List<AgentWorkflowTemplate> getAllTemplates() {
        List<AgentWorkflowTemplate> templates = workflowConfigMapper.selectAllTemplates();
        templates.forEach(template -> template.setStages(
                workflowConfigMapper.selectStagesByWorkflowCode(template.getCode())
        ));
        return templates;
    }

    public AgentWorkflowTemplate getTemplate(String code) {
        AgentWorkflowTemplate template = workflowConfigMapper.selectTemplateByCode(code);
        if (template != null) {
            template.setStages(workflowConfigMapper.selectStagesByWorkflowCode(code));
        }
        return template;
    }

    public AgentWorkflowTemplate saveTemplate(AgentWorkflowTemplate template) {
        long now = System.currentTimeMillis();
        if (template.getCreatedAt() == null) template.setCreatedAt(now);
        template.setUpdatedAt(now);
        if (template.getEnabled() == null) template.setEnabled(Boolean.TRUE);
        workflowConfigMapper.upsertTemplate(template);
        workflowConfigMapper.deleteStagesByWorkflowCode(template.getCode());
        if (template.getStages() != null) {
            for (AgentWorkflowStage stage : template.getStages()) {
                stage.setWorkflowCode(template.getCode());
                if (stage.getCreatedAt() == null) stage.setCreatedAt(now);
                stage.setUpdatedAt(now);
                workflowConfigMapper.insertStage(stage);
            }
        }
        return getTemplate(template.getCode());
    }

    public List<AgentWorkflowRouteRule> getAllRouteRules() {
        return workflowConfigMapper.selectAllRouteRules();
    }

    public AgentWorkflowRouteRule getRouteRule(String intentCode) {
        return workflowConfigMapper.selectRouteRuleByIntentCode(intentCode);
    }

    public AgentWorkflowRouteRule saveRouteRule(AgentWorkflowRouteRule rule) {
        long now = System.currentTimeMillis();
        if (rule.getCreatedAt() == null) rule.setCreatedAt(now);
        rule.setUpdatedAt(now);
        if (rule.getEnabled() == null) rule.setEnabled(Boolean.TRUE);
        workflowConfigMapper.upsertRouteRule(rule);
        return workflowConfigMapper.selectRouteRuleByIntentCode(rule.getIntentCode());
    }

    public List<WorkbenchLayoutConfig> getAllLayouts() {
        return workflowConfigMapper.selectAllLayouts();
    }

    public WorkbenchLayoutConfig getEffectiveLayout(String workflowCode, String stageCode) {
        WorkbenchLayoutConfig exact = workflowConfigMapper.selectEffectiveLayout(workflowCode, stageCode);
        if (exact != null) return exact;
        return workflowConfigMapper.selectEffectiveLayout("ops-service-flow", "L1");
    }

    public WorkbenchLayoutConfig saveLayout(WorkbenchLayoutConfig layout) {
        long now = System.currentTimeMillis();
        if (layout.getCreatedAt() == null) layout.setCreatedAt(now);
        layout.setUpdatedAt(now);
        if (layout.getEnabled() == null) layout.setEnabled(Boolean.TRUE);
        workflowConfigMapper.upsertLayout(layout);
        return getEffectiveLayout(layout.getWorkflowCode(), layout.getStageCode());
    }

    public AgentWorkflowStage resolveStage(String workflowCode, String stageCode) {
        if (workflowCode == null || workflowCode.isBlank()) {
            workflowCode = "ops-service-flow";
        }
        if (stageCode != null && !stageCode.isBlank()) {
            AgentWorkflowStage stage = workflowConfigMapper.selectStage(workflowCode, stageCode);
            if (stage != null) return stage;
        }
        return workflowConfigMapper.selectFirstStage(workflowCode);
    }

    public AgentWorkflowStage resolveStageByLevel(Integer level) {
        String stageCode = level == null || level <= 1 ? "L1" : "L" + level;
        AgentWorkflowStage stage = workflowConfigMapper.selectStage("ops-service-flow", stageCode);
        if (stage != null) return stage;
        return workflowConfigMapper.selectFirstStage("ops-service-flow");
    }

    public AgentWorkflowStage getNextStage(String workflowCode, Integer currentOrder) {
        return workflowConfigMapper.selectNextStage(workflowCode, currentOrder);
    }
}
