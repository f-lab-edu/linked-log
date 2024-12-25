package flab.Linkedlog.repository.post;

import flab.Linkedlog.entity.Post;

import java.util.List;

public interface PostRepositoryCustom {
    List<Post> findPostListInCategory(Long categoryId);

    List<Post> findPostListInCategoryContainKeyword(Long categoryId, String keyword);
    
}