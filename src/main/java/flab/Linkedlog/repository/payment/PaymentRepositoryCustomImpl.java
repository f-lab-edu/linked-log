package flab.Linkedlog.repository.payment;

import com.querydsl.jpa.impl.JPAQueryFactory;
import flab.Linkedlog.entity.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public class PaymentRepositoryCustomImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PaymentRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageImpl<Payment> findPaymentListByMember(Long memberId, Pageable pageable, Long lastSeenPaymentId) {
        QPayment payment = QPayment.payment;
        QMember member = QMember.member;

        // 기본 쿼리
        var query = queryFactory
                .selectFrom(payment)
                .join(payment.member, member).fetchJoin()
                .where(payment.member.id.eq(memberId));


        if (lastSeenPaymentId != null) {
            query.where(payment.id.gt(lastSeenPaymentId));
        }

        List<Payment> content = query
                .orderBy(payment.createdAt.desc(), payment.id.desc())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                        .select(payment.id.count())
                        .from(payment)
                        .where(payment.member.id.eq(memberId))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}
