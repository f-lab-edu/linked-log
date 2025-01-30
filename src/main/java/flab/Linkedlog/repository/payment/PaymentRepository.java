package flab.Linkedlog.repository.payment;

import flab.Linkedlog.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

    Payment findByOrderId(String orderId);

    void deleteAllByPaymentKey(String paymentKey);
    
}