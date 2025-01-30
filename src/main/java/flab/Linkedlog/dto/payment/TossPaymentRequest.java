package flab.Linkedlog.dto.payment;

import flab.Linkedlog.entity.enums.PaymentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class TossPaymentRequest {

    @NonNull
    private PaymentType paymentType;

    @NonNull
    private BigDecimal amount;

    @NotNull
    private String orderId;

    @NotNull
    private String paymentKey;


}