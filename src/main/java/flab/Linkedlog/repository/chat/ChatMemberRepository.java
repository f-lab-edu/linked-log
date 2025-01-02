package flab.Linkedlog.repository.chat;

import flab.Linkedlog.entity.ChatMember;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    int countByChatRoomId(Long chatRoomId);

    ChatMember findByChatRoomAndMember(ChatRoom chatRoom, Member member);

    List<ChatMember> findByChatRoomAndDeletedAtIsNull(ChatRoom chatRoom);

    Long countByChatRoomAndDeletedAtIsNull(ChatRoom chatRoom);

    List<ChatMember> findByChatRoom(ChatRoom chatRoom);


}
