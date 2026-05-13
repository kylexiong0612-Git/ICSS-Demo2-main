package com.icss.model;

import lombok.Data;

import java.util.List;

@Data
public class ServiceTask {
    private String id;
    private String customerId;
    private String agentId;
    private String requestSource;
    /** Pending | Processing | Suspended | Completed | Escalated */
    private String status;
    /** Low | Medium | High | Urgent */
    private String priority;
    private String category;
    private String summary;
    private String aiSuggestion;
    private String assignedTo;
    private Integer unreadCount;
    /** 1=一线 2=二线 */
    private Integer level;
    private String workflowCode;
    private String currentStageCode;
    private Integer currentStageOrder;
    private String sourceTaskId;
    private String intentCode;
    private String routeReason;
    private Double handoffConfidence;
    private List<String> tags;
    private Long createdAt;
    private Long updatedAt;
}
