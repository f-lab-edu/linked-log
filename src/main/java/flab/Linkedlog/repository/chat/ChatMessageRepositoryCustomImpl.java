package flab.Linkedlog.repository.chat;

import com.querydsl.jpa.impl.JPAQueryFactory;

import flab.Linkedlog.entity.ChatMessage;
import flab.Linkedlog.entity.QChatMessage;
import flab.Linkedlog.entity.QMember;

import java.util.List;

public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public ChatMessageRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }


    @Override
    public List<ChatMessage> findRecentChatMessageByChatRoomId(Long chatRoomId) {

        QChatMessage chatMessage = QChatMessage.chatMessage;
        QMember member = QMember.member;

        return queryFactory
                .selectFrom(chatMessage)
                .join(chatMessage.sender, member).fetchJoin()
                .where(chatMessage.chatRoom.id.eq(chatRoomId))
                .orderBy(chatMessage.createdAt.asc())
                .limit(20)
                .fetch();
    }

}
