package flab.Linkedlog.dto.post;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
public class PostDetailDto {


    @NotEmpty(message = "글 아이디")
    private Long id;

    @NotEmpty(message = "카테고리 아이디")
    private Long categoryId;

    @NotEmpty(message = "작성자")
    private String nickname;

    @NotEmpty(message = "글 제목")
    private String title;

    @NotEmpty(message = "글 내용")
    private String content;

    @NotEmpty(message = "작성일")
    private LocalDateTime createdAt;

    @NotEmpty(message = "조회수")
    private int viewes;

    @NotEmpty(message = "유료 금액")
    private int price;

    public PostDetailDto(Long id, String title, String content, LocalDateTime createdAt, String name, String nickName, int viewes, int price) {

    }


}
