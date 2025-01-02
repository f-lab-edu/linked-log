package flab.Linkedlog.repository.chat;

import flab.Linkedlog.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepositoryCustom {
    List<ChatMessage> findRecentChatMessageByChatRoomId(Long chatRoomId);
}
