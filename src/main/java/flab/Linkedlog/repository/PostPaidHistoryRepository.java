package flab.Linkedlog.repository;

import flab.Linkedlog.entity.PostPaidHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostPaidHistoryRepository extends JpaRepository<PostPaidHistory, Long> {
    boolean existsByMemberIdAndPostId(Long memberId, Long postId);

}
