package flab.Linkedlog.service.payment;

import flab.Linkedlog.dto.payment.PaymentListResponse;
import flab.Linkedlog.dto.payment.ProductListResponse;
import flab.Linkedlog.entity.Payment;
import flab.Linkedlog.repository.ProductRepository;
import flab.Linkedlog.repository.payment.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentQueryService {

    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    // 포인트 충전 상품 목록 조회
    public List<ProductListResponse> getProductList() {

        return productRepository.findAll().stream()
                .map(product -> {
                    return new ProductListResponse(
                            product.getId(),
                            product.getPrice(),
                            product.getProductName()
                    );
                })
                .collect(Collectors.toList());
    }


    public PageImpl<PaymentListResponse> getPaymentsHistory(Long memberId, Pageable pageable, Long lastSeenPaymentId) {
        PageImpl<Payment> paymentsPage = paymentRepository.findPaymentListByMember(memberId, pageable, lastSeenPaymentId);

        List<PaymentListResponse> paymentListResponse = paymentsPage.getContent().stream()
                .map(payment -> new PaymentListResponse(
                        payment.getId(),
                        payment.getPaymentKey(),
                        payment.getOrderName(),
                        payment.getCreatedAt(),
                        payment.getMember().getNickName(),
                        payment.getAmount()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(paymentListResponse, pageable, paymentsPage.getTotalElements());
    }
}
