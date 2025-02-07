package flab.Linkedlog.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
public class PaymentWidgetRequest {

    @NotNull
    private String clientKey;

    @NotNull
    private String customerKey;

    @NotNull
    private String orderId;

    @NotNull
    private String productName;

    @NotNull
    private BigDecimal price;

    @NotNull
    private String customerEmail;

    @NotNull
    private String customerPhone;

    @NotNull
    private String customerNickname;

}
