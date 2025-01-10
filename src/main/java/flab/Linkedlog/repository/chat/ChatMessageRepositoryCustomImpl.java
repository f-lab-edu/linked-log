package flab.Linkedlog.repository.chat;

import com.querydsl.jpa.impl.JPAQueryFactory;

import flab.Linkedlog.entity.ChatMessage;
import flab.Linkedlog.entity.QChatMessage;
import flab.Linkedlog.entity.QMember;

import java.util.Comparator;
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

        List<ChatMessage> messages = queryFactory
                .selectFrom(chatMessage)
                .join(chatMessage.sender, member).fetchJoin()
                .where(chatMessage.chatRoom.id.eq(chatRoomId))
                .orderBy(chatMessage.createdAt.desc())  // 시간 역순 정렬
                .limit(20)
                .fetch();

        messages.sort(Comparator.comparing(ChatMessage::getCreatedAt));  // 시간 순으로 정렬

        return messages;
    }

}
