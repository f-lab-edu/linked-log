package flab.Linkedlog.entity;

import flab.Linkedlog.entity.enums.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType paymentType;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "buyer_id")
    private Member member;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String orderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "product_id")
    private Product product;

    @Column(nullable = false)
    private String paymentKey;

    @Builder
    public Payment(PaymentType paymentType, BigDecimal amount, Member member, String orderId, String orderName, Product product, String paymentKey) {
        this.paymentType = paymentType;
        this.amount = amount;
        this.member = member;
        this.orderId = orderId;
        this.orderName = orderName;
        this.product = product;
        this.paymentKey = paymentKey;
    }

    public void discardTemporalOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void discardTemporalPaymentKey(String paymentKey) {
        this.paymentKey = paymentKey;
    }

}
