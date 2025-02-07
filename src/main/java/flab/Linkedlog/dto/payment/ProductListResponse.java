package flab.Linkedlog.dto.payment;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class ProductListResponse {

    @NotNull
    private Long id;

    @NotNull
    private BigDecimal price;

    @NotEmpty
    private String productName;
}
