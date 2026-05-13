package com.icss.model;

import lombok.Data;

import java.util.List;

@Data
public class AgentWorkflowStage {
    private Long id;
    private String workflowCode;
    private String code;
    private String name;
    private Integer stageOrder;
    private String roleLabel;
    private String description;
    private List<String> allowedActions;
    private Long createdAt;
    private Long updatedAt;
}
