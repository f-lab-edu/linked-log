package flab.Linkedlog.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentResponse {

    @NotNull
    private String mId;

    @NotNull
    private String lastTransactionKey;

    @NotNull
    private String paymentKey;

    @NotNull
    private String orderId;

    @NotNull
    private String orderName;

    @NotNull
    private Long totalAmount;

    @NotNull
    private String status;

    @NotNull
    private String requestedAt;

    @NotNull
    private String approvedAt;

}