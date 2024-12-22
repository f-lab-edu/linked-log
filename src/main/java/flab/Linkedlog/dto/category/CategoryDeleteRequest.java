package flab.Linkedlog.dto.category;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
public class CategoryDeleteRequest {

    @NotNull
    private Long id;
}
