package flab.Linkedlog.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ChatRoomMemberListResponse {

    @NotNull(message = "멤버 아이디")
    private Long id;

    @NotBlank(message = "닉네임")
    private String nickName;

    @NotBlank(message = "프로필 사진")
    private String profileUrl;

    @NotNull(message = "개설자 여부")
    private boolean isManager;

}
