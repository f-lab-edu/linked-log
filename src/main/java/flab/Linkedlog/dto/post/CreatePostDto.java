package flab.Linkedlog.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostDto {

    @NotBlank(message = "글 제목 입력")
    private String title;

    @NotBlank(message = "글 내용 입력")
    private String content;

}





