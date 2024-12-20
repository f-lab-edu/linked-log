package flab.Linkedlog.repository.post;


import flab.Linkedlog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    // 기존 메서드는 필요 없으며 QueryDSL로 처리
}