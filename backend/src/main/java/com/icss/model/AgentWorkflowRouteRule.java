package com.icss.model;

import lombok.Data;

@Data
public class AgentWorkflowRouteRule {
    private Long id;
    private String intentCode;
    private String intentName;
    private String targetWorkflowCode;
    private String entryStageCode;
    private String priorityStrategy;
    private Boolean enabled;
    private Long createdAt;
    private Long updatedAt;
}
