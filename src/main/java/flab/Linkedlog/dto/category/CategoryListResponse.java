package flab.Linkedlog.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class CategoryListResponse {

    private Long id;

    private String categoryName;
    
    private LocalDateTime createdAt;

}
