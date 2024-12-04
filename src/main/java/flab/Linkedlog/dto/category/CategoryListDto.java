package flab.Linkedlog.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@Schema(description = "카테고리 정보를 담고 있는 DTO")
public class CategoryListDto {

    public CategoryListDto(UUID id, String categoryName, LocalDateTime createdAt) {
        this.id = id;
        this.categoryName = categoryName;
        this.createdAt = createdAt;
    }

    @Schema(description = "카테고리 UUID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "카테고리 이름", example = "Technology")
    private String categoryName;

    @Schema(description = "생성일자", example = "2024-11-28T15:31:42.123")
    private LocalDateTime createdAt;

}
