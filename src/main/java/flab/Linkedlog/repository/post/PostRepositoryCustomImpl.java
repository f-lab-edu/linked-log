package flab.Linkedlog.repository.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.QCategory;
import flab.Linkedlog.entity.QMember;
import flab.Linkedlog.entity.QPost;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    public PostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
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
}