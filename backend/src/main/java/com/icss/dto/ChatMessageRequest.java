package com.icss.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class ChatMessageRequest {
    @NotBlank
    private String id;
    @NotBlank
    private String role;
    @NotBlank
    private String content;
    private String messageType = "text";
    private Map<String, Object> metadata;
    private Long createdAt;
}
