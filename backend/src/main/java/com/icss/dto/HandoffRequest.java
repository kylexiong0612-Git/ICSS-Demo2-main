package com.icss.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class HandoffRequest {
    @NotBlank
    private String customerId;

    @NotEmpty
    private List<ConversationMessageDto> messages;
}
