package flab.Linkedlog.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
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
public class PaymentListResponse {

    @NotNull
    private Long id;

    @NotNull
    private String paymentKey;

    @NotNull
    private String orderName;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private String nickname;

    @PositiveOrZero
    private BigDecimal amount;

}