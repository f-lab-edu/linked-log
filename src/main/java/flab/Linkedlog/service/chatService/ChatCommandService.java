package flab.Linkedlog.service.chatService;

import flab.Linkedlog.dto.chat.ChatRoomCreateRequest;
import flab.Linkedlog.entity.*;
import flab.Linkedlog.entity.enums.ChatRoomType;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.chat.ChatMemberRepository;
import flab.Linkedlog.repository.chat.ChatMessageRepository;
import flab.Linkedlog.repository.chat.ChatRoomRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 그룹 채팅방 개설
    public Long createGroupChatRoom(ChatRoomCreateRequest chatRoomCreateRequest, Long memberId) {

        Member manager = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        String password = chatRoomCreateRequest.getPassword() != null ? passwordEncoder.encode(chatRoomCreateRequest.getPassword()) : null;
        ChatRoom chatRoom = ChatRoom.builder()
                .member(manager)
                .title(chatRoomCreateRequest.getTitle())
                .password(password)
                .maxParticipants(chatRoomCreateRequest.getMaxParticipants())
                .chatRoomType(ChatRoomType.GROUP)
                .build();

        chatRoomRepository.save(chatRoom);

        ChatMember chatMember = ChatMember.builder()
                .member(manager)
                .chatRoom(chatRoom)
                .build();

        chatMemberRepository.save(chatMember);

        return chatRoom.getId();
    }

    // 1:1 채팅방 개설
    public Long createPersonalChatRoom(ChatRoomCreateRequest chatRoomCreateRequest, Long myId, Long receiverId) {

        Member manager = memberRepository.findById(myId).orElseThrow(EntityNotFoundException::new);
        Member receiver = memberRepository.findById(receiverId).orElseThrow(EntityNotFoundException::new);
        String title = manager.getNickName() + " : " + receiver.getNickName();

        ChatRoom chatRoom = ChatRoom.builder()
                .member(manager)
                .title(title)
                .maxParticipants(2)
                .chatRoomType(ChatRoomType.PERSONAL)
                .build();

        chatRoomRepository.save(chatRoom);

        ChatMember chatMemberManager = ChatMember.builder()
                .member(manager)
                .chatRoom(chatRoom)
                .build();
        ChatMember chatMemberReceiver = ChatMember.builder()
                .member(receiver)
                .chatRoom(chatRoom)
                .build();

        chatMemberRepository.save(chatMemberManager);
        chatMemberRepository.save(chatMemberReceiver);

        return chatRoom.getId();
    }


    // 채팅방에 참여
    public Long joinChatRoom(Long chatRoomId, Long memberId, String rawPassword) {

        Member joinMember = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(EntityNotFoundException::new);

        if (chatRoom.getPassword() != null) {
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if (!passwordEncoder.matches(rawPassword, chatRoom.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        if (chatRoom.getMaxParticipants() <= chatMemberRepository.countByChatRoomId(chatRoom.getId())) {
            throw new RuntimeException("채팅방에 최대 인원이 이미 도달했습니다.");
        }
        ChatMember chatMember = new ChatMember(joinMember, chatRoom);
        chatMemberRepository.save(chatMember);
        return chatMember.getId();
    }

    // 방장 위임
    public void delegateChatRoomManager(Long chatRoomId, Long delegatorId, Long delegateeId) {

        Member delegatorMember = memberRepository.findById(delegatorId).orElseThrow(EntityNotFoundException::new);
        Member delegateeMember = memberRepository.findById(delegateeId).orElseThrow(EntityNotFoundException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(EntityNotFoundException::new);
        ChatMember chatMember = chatMemberRepository.findByChatRoomAndMember(chatRoom, delegatorMember);

        if (!Objects.equals(delegatorMember.getId(), chatRoom.getMember().getId())) {
            throw new RuntimeException("위임 권한이 없습니다.");
        }

        chatRoom.delegateManager(delegateeMember);
        chatRoomRepository.save(chatRoom);
        chatMemberRepository.save(chatMember);

    }


    // 채팅방에서 퇴장
    public void leaveChatRoom(Long chatRoomId, Long memberId) {

        Member leaveMember = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(EntityNotFoundException::new);
        ChatMember chatMember = chatMemberRepository.findByChatRoomAndMember(chatRoom, leaveMember);

        if (chatMember == null) {
            throw new RuntimeException("참여자가 아닙니다.");
        }

        chatMember.deleteChatMember();
        chatMemberRepository.save(chatMember);
    }

    // 채팅방 삭제
    public void deleteChatRoom(Long chatRoomId, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId).orElseThrow(EntityNotFoundException::new);
        ChatMember chatMember = chatMemberRepository.findByChatRoomAndMember(chatRoom, member);

        if (!Objects.equals(member.getId(), chatRoom.getMember().getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        chatMember.deleteChatMember();
        chatRoom.deleteChatRoom();
        chatMemberRepository.save(chatMember);
        chatRoomRepository.save(chatRoom);

    }

//
//    // 채팅방에 메시지 전송
//    public Long sendMessage(Long chatRoomId, Member sender, String message) {
//
//        return chatMassage.getId();
//    }


}
