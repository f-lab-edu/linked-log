package flab.Linkedlog.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MyPageResponse {

    @NotEmpty(message = "프로필 이미지 경로")
    private String profileImageKey;

    @NotEmpty(message = "닉네임")
    private String nickname;


}


