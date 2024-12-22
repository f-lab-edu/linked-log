package flab.Linkedlog.entity;

import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.entity.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String nickName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberGrade memberGrade = MemberGrade.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus = MemberStatus.NORMAL;

    @Column(nullable = false)
    private BigDecimal cashPoint = BigDecimal.valueOf(0);

    private LocalDateTime deletedAt;


    @Builder
    public Member(String userId, String password, String nickName,
                  String email, String phone, MemberGrade memberGrade,
                  MemberStatus memberStatus, int cashPoint) {
        this.userId = userId;
        this.password = password;
        this.nickName = nickName;
        this.email = email;
        this.phone = phone;
        this.memberGrade = memberGrade != null ? memberGrade : MemberGrade.GENERAL;
        this.memberStatus = memberStatus != null ? memberStatus : MemberStatus.NORMAL;
        this.cashPoint = BigDecimal.valueOf(cashPoint != 0 ? cashPoint : 0);
    }
}
