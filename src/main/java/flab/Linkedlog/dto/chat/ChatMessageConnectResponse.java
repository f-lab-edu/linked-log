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
public class ChatMessageConnectResponse {

    @NotNull(message = "채팅방 아이디")
    private Long chatRoomId;

    @NotNull(message = "접속 및 해제 아이디")
    private Long memberId;

    @NotBlank(message = "내용")
    private String connectContent;

    @NotNull(message = "채팅 타입")
    private ChatMessageType chatMessageType;


}
