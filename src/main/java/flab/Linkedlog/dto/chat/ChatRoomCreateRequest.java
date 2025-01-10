package flab.Linkedlog.dto.chat;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatRoomCreateRequest {

    @NotBlank(message = "채팅방 제목 입력")
    private String title;

    @Nullable
    private String password;

    @Min(2)
    @Max(10)
    private int maxParticipants;

}
