package flab.Linkedlog.dto.post;

//import flab.Linkedlog.annotation.MultipleOfTen;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "글 제목 입력")
    private String title;

    @NotBlank(message = "글 내용 입력")
    private String content;

    @NotNull(message = "유료 포인트 입력")
    @DecimalMin(value = "0")
    @DecimalMax(value = "90")
    @Digits(integer = 2, fraction = 0)  // 소수점 허용 X
    //@MultipleOfTen
    private BigDecimal price;

}





