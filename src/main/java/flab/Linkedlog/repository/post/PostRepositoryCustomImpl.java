package flab.Linkedlog.repository.post;


import com.querydsl.jpa.impl.JPAQueryFactory;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.QCategory;
import flab.Linkedlog.entity.QMember;
import flab.Linkedlog.entity.QPost;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;


    public PostRepositoryCustomImpl(EntityManager em, EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(em);
        this.entityManager = entityManager;
    }

    @Override
    public List<Post> findPostListInCategory(Long categoryId) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QMember member = QMember.member;

        return queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.member, member).fetchJoin()
                .where(category.id.eq(categoryId))
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Post> findPostListInCategoryContainKeyword(Long categoryId, String keyword) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QMember member = QMember.member;

        return queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.member, member).fetchJoin()
                .where(
                        category.id.eq(categoryId)
                                .and(post.title.containsIgnoreCase(keyword)
                                        .or(post.content.containsIgnoreCase(keyword)))
                )
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    @Override
    @Transactional
    public void incrementViewCount(Long postId) {
        // EntityManager로 Post 객체 조회
        Post post = entityManager.find(Post.class, postId);
        if (post == null) {
            throw new EntityNotFoundException("Post not found");
        }

        // 엔티티 내 메서드로 조회수 증가
        post.incrementViews();

        // 조회수 증가 후, 변경된 Post 객체를 merge하여 영속성 컨텍스트에 반영
        entityManager.merge(post);
    }
}