package com.icss.model;

import lombok.Data;

import java.util.Map;

@Data
public class ChatMessage {
    private String id;
    private String customerId;
    private String taskId;
    /** user | bot | agent */
    private String role;
    private String content;
    private String messageType;
    private Map<String, Object> metadata;
    private Long createdAt;
}
