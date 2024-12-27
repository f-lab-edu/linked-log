package flab.Linkedlog.dto.chat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ChatRoomListResponse {

    @NotNull(message = "채팅방 아이디")
    private Long id;

    @NotNull(message = "채팅방 제목")
    private String title;

    @NotNull(message = "채팅방 개설자")
    private String nickName;

    @NotNull(message = "비밀번호 유무")
    private boolean hasPassword;

    @NotNull(message = "개설일")
    private LocalDateTime createdAt;

    @Positive(message = "허용 인원")
    private int maxParticipants;

}
