package flab.Linkedlog.repository.chat;

import flab.Linkedlog.entity.ChatMember;
import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    int countByChatRoomId(Long chatRoomId);

    ChatMember findByChatRoomAndMember(ChatRoom chatRoom, Member member);

}
