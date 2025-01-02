package flab.Linkedlog.handler;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.chat.ChatMessageConnectResponse;
import flab.Linkedlog.dto.chat.ChatMessageInnerRequest;
import flab.Linkedlog.dto.chat.ChatMessageRequest;
import flab.Linkedlog.dto.chat.ChatMessageResponse;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
import flab.Linkedlog.service.chatService.ChatCommandService;
import flab.Linkedlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class ChatMessageHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatCommandService chatCommandService;
    private final JwtUtil jwtUtil;

    private final Map<Long, Set<Long>> chatRoomUsers = new HashMap<>();
    private final ChatMemberRepository chatMemberRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;

//    // 채팅방 접속 처리
//    @MessageMapping("/chat.join/{chatRoomId}")
//    public void handleJoin(@DestinationVariable Long chatRoomId, String token) {
//        Long memberId = jwtUtil.getMemberIdFromToken(token);
//        Member member = memberRepository.findById(memberId).orElse(null);
//        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElse(null);
//        boolean isMemberInChatRoom = Optional.ofNullable(chatMemberRepository.findByChatRoomAndMember(chatRoom, member)).isPresent();
//
//
//        if (!isMemberInChatRoom) {
//            chatCommandService.joinChatRoom(chatRoomId, memberId, null);
//        }
//
//        chatRoomUsers.computeIfAbsent(chatRoomId, k -> new HashSet<>()).add(memberId);

//        messagingTemplate.convertAndSend("/topic/" + chatRoomId + "/users", chatRoomUsers.get(chatRoomId));
//
//        ChatMessageConnectResponse enterMessage = new ChatMessageConnectResponse(
//                chatRoomId,
//                memberId,
//                "has joined the chat.",
//                ChatMessageType.ENTER
//        );
//        messagingTemplate.convertAndSend("/topic/" + chatRoomId, enterMessage);
//
//    }

    //    // 채팅방 나가기 처리
//    @MessageMapping("/chat.exit/{chatRoomId}")
//    public void handleExit(@DestinationVariable Long chatRoomId, String token) {
//        Long memberId = jwtUtil.getMemberIdFromToken(token);
//        Set<Long> users = chatRoomUsers.get(chatRoomId);
//        if (users != null) {
//            users.remove(memberId);
//            messagingTemplate.convertAndSend("/topic/" + chatRoomId + "/users", users);
//        }
//
//        ChatMessageConnectResponse exitMessage = new ChatMessageConnectResponse(
//                chatRoomId,
//                memberId,
//                "has exit the chat.",
//                ChatMessageType.EXIT
//        );
//        messagingTemplate.convertAndSend("/topic/" + chatRoomId, exitMessage);
//
//    }

    //채팅방 접속
    @MessageMapping("/chat.connect/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ChatMessageConnectResponse> connectToChatRoom(
            @PathVariable Long chatRoomId,
            @RequestParam(required = false) String password,
            @RequestBody(required = false) String token) {

        ChatMessageConnectResponse response = chatCommandService.connectToChatRoom(chatRoomId, password, token);
        return ApiResponse.success(response);
    }


    // 채팅방에서 접속만 해제
    @MessageMapping("/chat.exit/{chatRoomId}")
    public void handleExit(@DestinationVariable Long chatRoomId, String token) {
        chatCommandService.disconnectFromChatRoom(chatRoomId, token);
    }


    // 메세지 전송
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/{chatRoomId}")
    public ChatMessageResponse handleChatMessage(@DestinationVariable Long chatRoomId, ChatMessageRequest chatMessageRequest) {
        // ChatMessage 에서 token 꺼내서 getMemberIdFromToken
        Long senderId = jwtUtil.getMemberIdFromToken(chatMessageRequest.getToken());
        //

        ChatMessageInnerRequest chatMessageInnerRequest = ChatMessageInnerRequest.builder()
                .chatRoomId(chatRoomId)
                .senderId(senderId)
                .chatContent(chatMessageRequest.getChatContent())
                .chatMessageType(chatMessageRequest.getChatMessageType())
                .build();

        return chatCommandService.sendMessage(chatMessageInnerRequest);
    }
}