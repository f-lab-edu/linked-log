package flab.Linkedlog.entity;

import flab.Linkedlog.entity.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    Long id;

    @Column(nullable = false)
    String title;

    @Column
    String password;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "manager_id")
    Member member;

    @Column(nullable = false)
    int maxParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType chatRoomType;

    LocalDateTime deletedAt;

    @Builder
    public ChatRoom(String title, String password, Member member, int maxParticipants, ChatRoomType chatRoomType) {
        this.title = title;
        this.password = password;
        this.member = member;
        this.maxParticipants = maxParticipants;
        this.chatRoomType = chatRoomType;
    }

    public void deleteChatRoom() {
        this.deletedAt = LocalDateTime.now();
    }

    public void delegateManager(Member member) {
        this.member = member;
    }

}
