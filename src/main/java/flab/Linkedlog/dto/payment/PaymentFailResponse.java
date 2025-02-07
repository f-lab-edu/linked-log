package flab.Linkedlog.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
public class PaymentFailResponse {


    @NonNull
    private String paymentType;

    @NotNull
    private Long productId;


}