package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.payment.*;
import flab.Linkedlog.service.payment.PaymentCommandService;
import flab.Linkedlog.service.payment.PaymentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    // 결제 상품 목록 조회
    @GetMapping(value = "/productlist")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductListResponse>> getProductList() {
        List<ProductListResponse> products = paymentQueryService.getProductList();
        return ApiResponse.success(products);
    }

    // 결제 요청, 임시 DB 저장
    @PostMapping("/request")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> requestPayment(
            @RequestBody PaymentRequest paymentRequest,
            CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        Long paymentId = paymentCommandService.requestPayment(paymentRequest, memberId);
        return ApiResponse.success(paymentId);
    }

    // 결제 위젯 렌더 인자 생성
    @GetMapping("/widgetinfo")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentWidgetRequest> getWidgetInfo(
            @RequestParam("productId") Long productId,
            @RequestParam("paymentId") Long paymentId,
            CustomUserDetails userDetails) {
        PaymentWidgetRequest paymentWidgetRequest = paymentCommandService.getPaymentWidgetRequest(productId, paymentId, userDetails);
        return ApiResponse.success(paymentWidgetRequest);
    }

    @PostMapping("/confirm")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentResponse> handlePaymentConfirm(
            @RequestBody TossPaymentRequest tossPaymentRequest,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws Exception {
        PaymentResponse paymentResponse = paymentCommandService.processPayment(
                tossPaymentRequest.getOrderId(),
                tossPaymentRequest.getPaymentKey(),
                tossPaymentRequest.getAmount(),
                userDetails);
        return ApiResponse.success(paymentResponse);

    }

    // 결제 실패
    @PostMapping("/fail")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PaymentFailResponse> paymentFail(
            @RequestBody PaymentFailResponse paymentFailResponse) {
        paymentCommandService.paymentFail(paymentFailResponse);
        return ApiResponse.success(null);
    }

    // 결제 내역 조회
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PageImpl> getUserPayment(
            CustomUserDetails userDetails,
            Pageable pageable,
            @RequestParam(required = false) Long lastSeenPaymentId) {

        Long memberId = userDetails.getMemberId();

        if (pageable.getPageSize() <= 0) {
            PageImpl<PaymentListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return ApiResponse.success(emptyPage);
        }

        PageImpl<PaymentListResponse> payments = paymentQueryService.getPaymentsHistory(memberId, pageable, lastSeenPaymentId);
        return ApiResponse.success(payments);
    }

}
