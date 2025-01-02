package flab.Linkedlog.entity;

import flab.Linkedlog.entity.enums.ChatMessageType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "chat_room_id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "sender_id")
    private Member sender;

    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatMessageType chatMessageType = ChatMessageType.SEND;

    @Builder
    public ChatMessage(ChatRoom chatRoom, Member sender, String message, ChatMessageType chatMessageType) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.message = message;
        this.chatMessageType = chatMessageType;
    }
}



