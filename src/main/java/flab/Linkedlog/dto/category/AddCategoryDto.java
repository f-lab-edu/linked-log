package flab.Linkedlog.dto.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCategoryDto {

    @NotEmpty(message = "카테고리 이름을 입력하세요.")
    private String categoryName;

}
