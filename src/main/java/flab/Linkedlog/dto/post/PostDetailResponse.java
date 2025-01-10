package flab.Linkedlog.dto.post;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
public class PostDetailResponse {


    @NotNull(message = "글 아이디")
    private Long id;

    @NotNull(message = "카테고리 아이디")
    private Long categoryId;

    @NotEmpty(message = "작성자")
    private String nickname;

    @NotEmpty(message = "글 제목")
    private String title;

    @NotEmpty(message = "글 내용")
    private String content;

    @NotNull(message = "작성일")
    private LocalDateTime createdAt;

    @PositiveOrZero(message = "조회수")
    private int viewes;

    @PositiveOrZero(message = "유료 금액")
    private BigDecimal price;

    private List<String> images;


}
