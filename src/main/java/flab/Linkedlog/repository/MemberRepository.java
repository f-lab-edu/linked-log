package flab.Linkedlog.repository;

import flab.Linkedlog.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    // 사용자 정의 메서드 정의
    Optional<Member> findByUserId(String userId); // userId로 검색


}


