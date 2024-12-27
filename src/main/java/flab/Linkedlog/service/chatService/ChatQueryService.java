package flab.Linkedlog.service.chatService;

import flab.Linkedlog.dto.chat.ChatRoomListResponse;
import flab.Linkedlog.dto.chat.ChatRoomMemberListResponse;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatMessageRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatQueryService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

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

//
//    // 채팅방 상세 조회
//    public List<ChatRoomDetailResponse> getChatRoomDetail(Long id) {
//    }


}
