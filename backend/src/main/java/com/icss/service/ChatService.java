package com.icss.service;

import com.icss.dto.ChatMessageRequest;
import com.icss.mapper.ChatMessageMapper;
import com.icss.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageMapper chatMessageMapper;

    public List<ChatMessage> getHistory(String customerId) {
        return chatMessageMapper.selectByCustomerId(customerId);
    }

    public ChatMessage saveMessage(String customerId, ChatMessageRequest req) {
        ChatMessage msg = new ChatMessage();
        msg.setId(req.getId());
        msg.setCustomerId(customerId);
        msg.setRole(req.getRole());
        msg.setContent(req.getContent());
        msg.setMessageType(req.getMessageType() != null ? req.getMessageType() : "text");
        msg.setMetadata(req.getMetadata());
        msg.setCreatedAt(req.getCreatedAt() != null ? req.getCreatedAt() : System.currentTimeMillis());
        chatMessageMapper.insert(msg);
        return msg;
    }
}
