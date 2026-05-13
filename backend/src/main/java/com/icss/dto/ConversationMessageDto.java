package com.icss.dto;

import lombok.Data;

import java.util.Map;

@Data
public class ConversationMessageDto {
    private String id;
    private String role;
    private String content;
    private String type;
    private Map<String, Object> metadata;
    private Long timestamp;
}
