package flab.Linkedlog.repository.post;

import flab.Linkedlog.entity.Post;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {
    PageImpl<Post> findPostListInCategory(Long categoryId, Pageable pageable);

    PageImpl<Post> findPostListInCategoryContainKeyword(Long categoryId, String keyword, Pageable pageable);

}