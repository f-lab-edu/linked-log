package flab.Linkedlog.dto.post;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostListDto {

    @NotNull(message = "글 아이디")
    private Long id;

    @NotNull(message = "글 제목")
    private String title;

    @NotNull(message = "글 내용")
    private String content;

    @NotNull(message = "작성일")
    private LocalDateTime createdAt;

    @NotNull(message = "카테고리 이름")
    private String category;

    @NotNull(message = "작성자")
    private String nickname;

    @Positive(message = "조회수는 0 이상의 값이어야 합니다")
    private int viewes;

    @Positive(message = "유료 금액은 0 이상의 값이어야 합니다")
    private BigDecimal price;

}
