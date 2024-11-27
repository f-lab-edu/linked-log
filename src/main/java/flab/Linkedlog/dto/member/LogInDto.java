package flab.Linkedlog.dto.member;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LogInDto {

    @NotEmpty(message = "아이디를 입력하세요.")
    private String userId;

    @NotEmpty(message = "비밀번호를 입력하세요.")
    private String password;

}
