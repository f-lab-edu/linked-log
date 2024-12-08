package flab.Linkedlog.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class MyPageDto {
    
    @NotEmpty(message = "프로필 이미지 경로")
    private String profileImageKey;

    @NotEmpty(message = "닉네임")
    private String nickname;


}


