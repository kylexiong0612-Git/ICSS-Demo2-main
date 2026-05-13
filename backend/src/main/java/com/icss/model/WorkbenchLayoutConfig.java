package com.icss.model;

import lombok.Data;

@Data
public class WorkbenchLayoutConfig {
    private String code;
    private String name;
    private String workflowCode;
    private String stageCode;
    private Object regions;
    private Boolean enabled;
    private Long createdAt;
    private Long updatedAt;
}
