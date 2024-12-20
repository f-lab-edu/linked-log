package flab.Linkedlog.repository.post;

import flab.Linkedlog.entity.Post;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPostListInCategory(Long categoryId);

    List<Post> findPostListInCategoryContainKeyword(Long categoryId, String keyword);

    void incrementViewCount(Long postId);
}