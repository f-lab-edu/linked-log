package flab.Linkedlog.dto.chat;

import flab.Linkedlog.entity.enums.ChatMessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatMessageRequest {

    @NotNull(message = "작성자 토큰")
    private String token;

    @NotBlank(message = "채팅 내용")
    private String chatContent;

    @NotNull(message = "채팅 타입")
    private ChatMessageType chatMessageType;


}
