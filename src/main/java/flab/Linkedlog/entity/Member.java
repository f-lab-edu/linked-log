package flab.Linkedlog.entity;

import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.entity.enums.MemberStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private MemberGrade memberGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus memberStatus;

    @Column(nullable = false)
    private int cashPoint;

    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        this.cashPoint = 0;
        this.memberStatus = MemberStatus.NORMAL;
        this.memberGrade = MemberGrade.GENERAL;
    }

    @Builder
    public Member(String userId, String password, String nickName,
                  String email, String phone) {
        this.userId = userId;
        this.password = password;
        this.nickName = nickName;
        this.email = email;
        this.phone = phone;
    }
}
