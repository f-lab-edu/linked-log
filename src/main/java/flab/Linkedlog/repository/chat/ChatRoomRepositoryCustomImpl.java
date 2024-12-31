package flab.Linkedlog.repository.chat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import flab.Linkedlog.entity.*;
import flab.Linkedlog.entity.enums.ChatRoomType;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatRoomRepositoryCustomImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ChatRoomRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<ChatRoom> findGroupChatRooms() {
        QChatRoom chatRoom = QChatRoom.chatRoom;


        return queryFactory
                .selectFrom(chatRoom)
                .where(chatRoom.chatRoomType.eq(ChatRoomType.GROUP))
                .orderBy(chatRoom.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Member> findChatRoomMembers(Long chatRoomId) {

        QChatMember chatMember = QChatMember.chatMember;

        return queryFactory
                .select(chatMember.member)
                .from(chatMember)
                .join(chatMember.chatRoom)
                .where(chatMember.chatRoom.id.eq(chatRoomId)
                        .and(chatMember.deletedAt.isNull()))
                .fetch();
    }

}
