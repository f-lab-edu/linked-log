package flab.Linkedlog.repository.postImage;

import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.PostImage;
import flab.Linkedlog.repository.post.PostRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    @Query("SELECT pi FROM PostImage pi WHERE pi.post.id = :postId")
    List<PostImage> findAllByPostId(@Param("postId") Long postId);
}