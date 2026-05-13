package com.icss.mapper;

import com.icss.model.AgentWorkflowRouteRule;
import com.icss.model.AgentWorkflowStage;
import com.icss.model.AgentWorkflowTemplate;
import com.icss.model.WorkbenchLayoutConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowConfigMapper {
    List<AgentWorkflowTemplate> selectAllTemplates();

    AgentWorkflowTemplate selectTemplateByCode(@Param("code") String code);

    List<AgentWorkflowStage> selectStagesByWorkflowCode(@Param("workflowCode") String workflowCode);

    AgentWorkflowStage selectStage(@Param("workflowCode") String workflowCode,
                                   @Param("stageCode") String stageCode);

    AgentWorkflowStage selectFirstStage(@Param("workflowCode") String workflowCode);

    AgentWorkflowStage selectNextStage(@Param("workflowCode") String workflowCode,
                                       @Param("currentOrder") Integer currentOrder);

    int upsertTemplate(AgentWorkflowTemplate template);

    int deleteStagesByWorkflowCode(@Param("workflowCode") String workflowCode);

    int insertStage(AgentWorkflowStage stage);

    List<AgentWorkflowRouteRule> selectAllRouteRules();

    AgentWorkflowRouteRule selectRouteRuleByIntentCode(@Param("intentCode") String intentCode);

    int upsertRouteRule(AgentWorkflowRouteRule rule);

    List<WorkbenchLayoutConfig> selectAllLayouts();

    WorkbenchLayoutConfig selectEffectiveLayout(@Param("workflowCode") String workflowCode,
                                                @Param("stageCode") String stageCode);

    int upsertLayout(WorkbenchLayoutConfig layout);
}
