package flab.Linkedlog.service.payment;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.config.TossPaymentConfig;
import flab.Linkedlog.dto.payment.PaymentFailResponse;
import flab.Linkedlog.dto.payment.PaymentRequest;
import flab.Linkedlog.dto.payment.PaymentResponse;
import flab.Linkedlog.dto.payment.PaymentWidgetRequest;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.Payment;
import flab.Linkedlog.entity.Product;
import flab.Linkedlog.entity.enums.PaymentType;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.ProductRepository;
import flab.Linkedlog.repository.payment.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final TossPaymentConfig tossConfig;
    private final RestTemplate restTemplate;

    private final String TOSS_API_URL = "https://api.tosspayments.com/v1/payments/confirm";


    public Long requestPayment(PaymentRequest paymentRequest, Long memberId) {
        // 실제 paymentKey와 orderId는 위젯으로 결제 성공 후 생성하여 임시 문자열을 대체한다.
        String temporalPaymentKey = "temporalPaymentKey";
        String temporalOrderId = "temporalOrderId";

        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);
        Product product = productRepository.findById(paymentRequest.getProductId()).orElseThrow(EntityNotFoundException::new);
        PaymentType paymentType = PaymentType.fromString(paymentRequest.getPaymentType());

        Payment payment = Payment.builder()
                .paymentType(paymentType)
                .amount(product.getPrice())
                .paymentKey(temporalPaymentKey)
                .orderId(temporalOrderId)
                .orderName(product.getProductName())
                .member(member)
                .product(product)
                .build();

        paymentRepository.save(payment);
        return payment.getId();
    }


    public PaymentWidgetRequest getPaymentWidgetRequest(Long productId, Long paymentId, CustomUserDetails userDetails) {

        String tossClientKey = tossConfig.getTestClientKey();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String customerKey = UUID.randomUUID().toString();
        String orderId = UUID.randomUUID().toString();

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        payment.discardTemporalOrderId(orderId);

        Member member = memberRepository.findById(userDetails.getMemberId()).orElseThrow();
        String phoneNumber = member.getPhone().replace("-", "");

        return PaymentWidgetRequest.builder()
                .clientKey(tossClientKey)
                .customerKey(customerKey)
                .orderId(orderId)
                .price(product.getPrice())
                .productName(product.getProductName())
                .customerEmail(member.getEmail())
                .customerPhone(phoneNumber)
                .customerNickname(member.getNickName())
                .build();
    }

    // 결제 금액 검증 및 승인 요청을 처리하는 메서드
    public PaymentResponse processPayment(String orderId, String paymentKey, BigDecimal amount, CustomUserDetails userDetails) throws Exception {
        // 1. 금액 검증
        boolean isAmountValid = validateAmount(orderId, amount);
        if (!isAmountValid) {
            throw new Exception("금액이 일치하지 않습니다.");
        }

        // 2. 결제 승인 요청
        PaymentResponse paymentResponse = confirmPayment(paymentKey, orderId, amount);
        if (paymentResponse == null || !paymentResponse.getStatus().equals("DONE")) {
            throw new Exception("결제 승인에 실패했습니다.");
        }

        // 3. DB payment에 paymentKey를 최종 단계에서 저장
        Payment payment = paymentRepository.findByOrderId(orderId);
        payment.discardTemporalPaymentKey(paymentKey);

        // 4. 회원 포인트 증가
        BigDecimal paymentAmount = paymentResponse.getTotalAmount() != null ?
                BigDecimal.valueOf(paymentResponse.getTotalAmount().longValue()) :
                BigDecimal.ZERO;

        Long memberId = userDetails.getMemberId();
        Member member = memberRepository.findById(memberId).orElseThrow();
        member.increaseCashPoint(paymentAmount);
        memberRepository.save(member);

        return paymentResponse;
    }


    public boolean validateAmount(String orderId, BigDecimal amount) {

        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            return false;
        }
        return payment.getAmount().compareTo(amount) == 0;
    }

    // 결제 승인 요청
    public PaymentResponse confirmPayment(String paymentKey, String orderId, BigDecimal amount) {
        String tossSecretKey = tossConfig.getTestSecretKey() + ":";
        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey).getBytes());
        // API 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodedSecretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 요청 바디 생성
        String requestBody = String.format(
                "{\"paymentKey\":\"%s\", \"orderId\":\"%s\", \"amount\":%f}",
                paymentKey, orderId, amount
        );

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        // Toss 결제 승인 API 호출
        try {
            ResponseEntity<PaymentResponse> response = restTemplate.exchange(
                    TOSS_API_URL,
                    HttpMethod.POST,
                    entity,
                    PaymentResponse.class
            );
            log.info("Toss API Response: " + response.getBody());
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error during payment approval", e);
        }
    }

    public PaymentFailResponse paymentFail(PaymentFailResponse paymentFailResponse) {
        String temporalPaymentKey = "temporalPaymentKey";
        paymentRepository.deleteAllByPaymentKey(temporalPaymentKey);
        return paymentFailResponse;
    }
}
