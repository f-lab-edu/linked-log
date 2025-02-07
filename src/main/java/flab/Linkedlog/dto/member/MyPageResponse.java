package flab.Linkedlog.dto.member;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class MyPageResponse {

    @NotNull
    private Long memberId;

    @NotEmpty(message = "프로필 이미지 경로")
    private String profileImageKey;

    @NotEmpty(message = "닉네임")
    private String nickname;

    @NotNull(message = "포인트")
    private BigDecimal point;


}


