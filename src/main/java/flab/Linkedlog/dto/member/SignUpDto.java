package flab.Linkedlog.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class SignUpDto {

    @NotEmpty(message = "아이디는 필수입니다.")
    private String userId;

    @NotEmpty(message = "비밀번호는 필수입니다.")
    private String password;

    @NotEmpty(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotEmpty(message = "이메일은 필수입니다.")
    private String email1;

    @NotEmpty(message = "이메일은 필수입니다.")
    private String email2;

    @NotEmpty(message = "전화번호는 필수입니다.")
    private String phone1;
    @NotEmpty(message = "전화번호는 필수입니다.")
    private String phone2;
    @NotEmpty(message = "전화번호는 필수입니다.")
    private String phone3;


}
