package com.icss.mapper;

import com.icss.model.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    List<ChatMessage> selectByCustomerId(String customerId);

    int insert(ChatMessage message);
}
