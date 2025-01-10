package flab.Linkedlog.dto.chat;

import flab.Linkedlog.entity.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatMessageResponse {
    @NotNull(message = "메세지 아이디")
    private Long id;

    @NotNull(message = "채팅방 아이디")
    private Long chatRoomId;

    @NotNull(message = "작성자 아이디")
    private Long senderId;

    @NotBlank(message = "작성자 닉네임")
    private String senderNickname;

    @NotBlank(message = "채팅 내용")
    private String chatContent;

    @NotNull(message = "채팅 타입")
    private ChatMessageType chatMessageType;

    @NotNull(message = "전송 시간")
    private LocalDateTime createdAt;
}
