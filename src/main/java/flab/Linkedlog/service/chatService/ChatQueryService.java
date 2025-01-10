package flab.Linkedlog.service.chatService;

import flab.Linkedlog.dto.chat.ChatMessageResponse;
import flab.Linkedlog.dto.chat.ChatRoomDetailResponse;
import flab.Linkedlog.dto.chat.ChatRoomListResponse;
import flab.Linkedlog.dto.chat.ChatRoomMemberListResponse;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatMessageRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
import flab.Linkedlog.service.MemberService;
import flab.Linkedlog.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final MemberService memberService;

    Map<Long, Set<Long>> chatRoomUsers = new HashMap<>();

    // 채팅방 목록 조회
    public List<ChatRoomListResponse> getChatRoomsList() {
        return chatRoomRepository.findGroupChatRooms().stream()
                .map(chatroom -> {
                    boolean hasPassword = chatroom.getPassword() != null;
                    return new ChatRoomListResponse(
                            chatroom.getId(),
                            chatroom.getTitle(),
                            chatroom.getMember().getNickName(),
                            hasPassword,
                            chatroom.getCreatedAt(),
                            chatroom.getMaxParticipants()
                    );
                })
                .collect(Collectors.toList());
    }

    // 채팅방 참여자 목록 조회
    public List<ChatRoomMemberListResponse> getChatRoomMemberList(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방이 존재하지 않습니다."));

        Long managerId = chatRoom.getMember().getId();

        return chatRoomRepository.findChatRoomMembers(chatRoomId).stream()
                .map(member -> {
                    boolean isManager = managerId.equals(member.getId());
                    return new ChatRoomMemberListResponse(
                            member.getId(),
                            member.getNickName(),
                            member.getProfileImage(),
                            isManager
                    );
                })
                .collect(Collectors.toList());
    }

    // 채팅방 상세 조회
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomId, String token) {

        Long memberId = jwtUtil.getMemberIdFromTokenOrContext(token);
        // ChatRoom과 Member 검증
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid chat room ID"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid member ID"));

        // 채팅방에 참가한 사용자 목록 가져오기
        List<ChatRoomMemberListResponse> joinedUserInfo = getChatRoomMemberList(chatRoom.getId());

        // 접속 중인 사용자 목록 가져오기 (예: chatRoomUsers에서 가져온 접속 중인 사용자 IDs)
        Set<Long> connectedUserIds = chatRoomUsers.getOrDefault(chatRoomId, new HashSet<>());

        // 최근 메시지 가져오기 (예: 최근 20개)
        List<ChatMessageResponse> recentMessages = chatMessageRepository
                .findRecentChatMessageByChatRoomId(chatRoomId)
                .stream()
                .map(message -> {
                    return ChatMessageResponse.builder()  // builder 패턴으로 객체 생성
                            .id(message.getId())
                            .chatRoomId(message.getChatRoom().getId())
                            .senderId(message.getSender().getId())
                            .senderNickname(message.getSender().getNickName())
                            .chatContent(message.getMessage())
                            .chatMessageType(message.getChatMessageType())
                            .createdAt(message.getCreatedAt())
                            .build();  // 객체 생성 후 반환
                })
                .collect(Collectors.toList());

        // ChatRoomDetailResponse 반환
        return ChatRoomDetailResponse.builder()
                .chatRoomId(chatRoomId)
                .title(chatRoom.getTitle())
                .joinedUserInfo(joinedUserInfo)  // 채팅방에 참가한 사용자 정보
                .connectedUserIds(connectedUserIds)  // 접속 중인 사용자 정보
                .recentMessages(recentMessages)  // 최근 메시지 정보
                .build();
    }

}
