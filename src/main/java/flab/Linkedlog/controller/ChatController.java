package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.chat.*;
import flab.Linkedlog.service.chatService.ChatCommandService;
import flab.Linkedlog.service.chatService.ChatQueryService;
import flab.Linkedlog.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatCommandService chatCommandService;
    private final ChatQueryService chatQueryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final JwtUtil jwtUtil;
    Logger logger = LoggerFactory.getLogger(ChatController.class);

    // 단체 채팅방 개설
    @PostMapping(value = "/create/group")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> createGroupChatRoom(
            @Valid @RequestBody ChatRoomCreateRequest chatRoomCreateRequest,
            CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();

        Long chatRoomId = chatCommandService.createGroupChatRoom(chatRoomCreateRequest, memberId);
        return ApiResponse.success(chatRoomId);
    }

//
//    // 1:1 채팅방 개설
//    @PostMapping(value = "/create/personal/{receiverId}")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<Long> createPersonalChatRoom(
//            @Valid @RequestBody ChatRoomCreateRequest chatRoomCreateRequest,
//            @PathVariable Long receiverId,
//            CustomUserDetails userDetails) {
//        Long myId = userDetails.getMemberId();
//
//        Long chatRoomId = chatCommandService.createPersonalChatRoom(chatRoomCreateRequest, myId, receiverId);
//        return ApiResponse.success(chatRoomId);
//    }
//
//    // 채팅방 참여(depreciated)
//    @PostMapping(value = "/join/{chatRoomId}")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<Long> joinChatRoom(@PathVariable Long chatRoomId,
//                                          @RequestParam(required = false) String password,
//                                          CustomUserDetails userDetails) {
//        Long memberId = userDetails.getMemberId();
//
//        Long joinMemberId = chatCommandService.joinChatRoom(chatRoomId, memberId, password);
//        return ApiResponse.success(joinMemberId);
//    }


    //채팅방 접속
    @PostMapping("/chat.connect/{chatRoomId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ChatMessageConnectResponse> connectToChatRoom(
            @PathVariable Long chatRoomId,
            @RequestBody ChatRoomPasswordRequest request,
            @RequestHeader("Authorization") String token) {

        ChatMessageConnectResponse response = chatCommandService.connectToChatRoom(chatRoomId, request.getPassword(), token);
        return ApiResponse.success(response);
    }


//    // 방장 위임
//    @PostMapping(value = "/chatroom/{chatRoomId}/delegate/{memberId}")
//    @PreAuthorize("isAuthenticated()")
//    public ApiResponse<Long> delegateChatRoomManager(
//            @PathVariable Long chatRoomId,
//            @PathVariable Long memberId,
//            CustomUserDetails userDetails) {
//        Long manageMemberId = userDetails.getMemberId();
//
//        chatCommandService.delegateChatRoomManager(chatRoomId, manageMemberId, memberId);
//        return ApiResponse.success(memberId);
//    }

    // 채팅방 퇴장
    @PostMapping(value = "/chatroom/{chatRoomId}/leave")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> leaveChatRoom(
            @PathVariable Long chatRoomId,
            @RequestHeader("Authorization") String token,
            CustomUserDetails userDetails) {
        Long leaveMemberId = userDetails.getMemberId();

        chatCommandService.leaveChatRoom(chatRoomId, leaveMemberId, token);
        //messagingTemplate.convertAndSend("/app/chat.leave/" + chatRoomId, leaveMemberId); // handleLeave 호출
        return ApiResponse.success(leaveMemberId);
    }

    // 채팅방 삭제
    @PostMapping(value = "/chatroom/{chatRoomId}/delete")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> deleteChatRoom(@PathVariable Long chatRoomId,
                                            CustomUserDetails userDetails) {
        Long manageMemberId = userDetails.getMemberId();

        chatCommandService.deleteChatRoom(chatRoomId, manageMemberId);
        return ApiResponse.success(chatRoomId);
    }

    // 채팅방 목록
    @GetMapping("/chatroom")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<ChatRoomListResponse>> getChatRoomsList() {
        List<ChatRoomListResponse> chatRooms = chatQueryService.getChatRoomsList();
        return ApiResponse.success(chatRooms);
    }

    // 채팅방 참여자 목록
    @GetMapping("/chatroom/{chatRoomId}/member")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ChatRoomMemberListResponse>> getChatRoomMemberList(@PathVariable Long chatRoomId) {
        List<ChatRoomMemberListResponse> members = chatQueryService.getChatRoomMemberList(chatRoomId);
        return ApiResponse.success(members);
    }

    // 접속중인 채팅방 상세
    @GetMapping("/chatroom/{chatRoomId}/info")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ChatRoomDetailResponse> getChatRoomInfo(
            @PathVariable Long chatRoomId,
            @RequestHeader("Authorization") String token) {
        logger.info("토큰 검증 : " + token);
        ChatRoomDetailResponse chatRoomDetailResponse = chatQueryService.getChatRoomDetail(chatRoomId, token);
        return ApiResponse.success(chatRoomDetailResponse);
    }

    // 채팅방 상세
}
