package com.icss.model;

import lombok.Data;

import java.util.List;

@Data
public class HandoffAnalysis {
    private Boolean handoffNeeded;
    private String intentCode;
    private String intentName;
    private String summary;
    private String suggestion;
    private List<String> tags;
    private String targetWorkflowCode;
    private String targetStageCode;
    private Double confidence;
    private String routeReason;
}
