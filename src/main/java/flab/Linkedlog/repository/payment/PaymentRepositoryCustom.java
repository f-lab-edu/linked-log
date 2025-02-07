package flab.Linkedlog.repository.payment;

import flab.Linkedlog.entity.Payment;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PaymentRepositoryCustom {

    PageImpl<Payment> findPaymentListByMember(Long memberId, Pageable pageable, Long lastSeenPaymentId);

}
