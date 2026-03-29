package com.icss.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaultTaskRequest {
    private String relatedTaskId;
    private String faultType;
    @NotBlank
    private String description;
    private String affectedScope;
    /** Low | Medium | High | Urgent */
    @NotBlank
    private String urgency;
}
