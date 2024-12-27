package flab.Linkedlog.repository.chat;

import flab.Linkedlog.entity.ChatRoom;
import flab.Linkedlog.entity.Member;

import java.util.List;

public interface ChatRoomRepositoryCustom {

    List<ChatRoom> findGroupChatRooms();

    List<Member> findChatRoomMembers(Long chatRoomId);

}
