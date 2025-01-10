package flab.Linkedlog.handler;

import flab.Linkedlog.dto.chat.*;
import flab.Linkedlog.service.chatService.ChatCommandService;
import flab.Linkedlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final ChatCommandService chatCommandService;
    private final JwtUtil jwtUtil;


    // 채팅방에서 접속만 해제
    @MessageMapping("/chat.exit/{chatRoomId}")
    public void handleExit(@DestinationVariable Long chatRoomId, String token) {
        chatCommandService.disconnectFromChatRoom(chatRoomId, token);
    }


    // 메세지 전송
    @MessageMapping("/chat/{chatRoomId}/sendMessage")
    @SendTo("/topic/{chatRoomId}")
    public ChatMessageResponse handleChatMessage(@DestinationVariable Long chatRoomId, @Payload ChatMessageRequest chatMessageRequest) {
        Long senderId = jwtUtil.getMemberIdFromToken(chatMessageRequest.getToken());

        ChatMessageInnerRequest chatMessageInnerRequest = ChatMessageInnerRequest.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .chatContent(chatMessageRequest.getChatContent())
                .chatMessageType(chatMessageRequest.getChatMessageType())
                .build();

        return chatCommandService.sendMessage(chatMessageInnerRequest);
    }
}