package com.icss.controller;

import com.icss.dto.ApiResponse;
import com.icss.dto.ChatMessageRequest;
import com.icss.model.ChatMessage;
import com.icss.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{customerId}")
    public ApiResponse<List<ChatMessage>> getHistory(@PathVariable String customerId) {
        return ApiResponse.success(chatService.getHistory(customerId));
    }

    @PostMapping("/{customerId}/messages")
    public ApiResponse<ChatMessage> saveMessage(@PathVariable String customerId,
                                                @Valid @RequestBody ChatMessageRequest req) {
        return ApiResponse.success(chatService.saveMessage(customerId, req));
    }
}
