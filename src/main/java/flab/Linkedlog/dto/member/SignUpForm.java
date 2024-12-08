package flab.Linkedlog.dto.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpForm {

    private String userId;
    private String password;
    private String nickname;
    private String email1;
    private String email2;
    private String phone1;
    private String phone2;
    private String phone3;
    private MultipartFile profileImage;

    // Convert to SignUpDto
    public SignUpDto toDto() {
        return new SignUpDto(userId, password, nickname, email1, email2, phone1, phone2, phone3);
    }
}