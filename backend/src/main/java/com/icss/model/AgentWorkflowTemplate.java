package com.icss.model;

import lombok.Data;

import java.util.List;

@Data
public class AgentWorkflowTemplate {
    private String code;
    private String name;
    private String description;
    private List<String> categoryScope;
    private Boolean enabled;
    private List<AgentWorkflowStage> stages;
    private Long createdAt;
    private Long updatedAt;
}
