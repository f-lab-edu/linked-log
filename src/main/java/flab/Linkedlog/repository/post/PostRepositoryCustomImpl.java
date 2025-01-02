package flab.Linkedlog.repository.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.QCategory;
import flab.Linkedlog.entity.QMember;
import flab.Linkedlog.entity.QPost;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    public PostRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public PageImpl<Post> findPostListInCategory(Long categoryId, Pageable pageable) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QMember member = QMember.member;

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.member, member).fetchJoin()
                .where(category.id.eq(categoryId))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                        .select(post.id.count())
                        .from(post)
                        .where(category.id.eq(categoryId))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);

    }

    @Override
    public PageImpl<Post> findPostListInCategoryContainKeyword(Long categoryId, String keyword, Pageable pageable) {
        QPost post = QPost.post;
        QCategory category = QCategory.category;
        QMember member = QMember.member;

        List<Post> content = queryFactory
                .selectFrom(post)
                .join(post.category, category).fetchJoin()
                .join(post.member, member).fetchJoin()
                .where(
                        category.id.eq(categoryId)
                                .and(post.title.containsIgnoreCase(keyword)
                                        .or(post.content.containsIgnoreCase(keyword)))
                )
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = Optional.ofNullable(queryFactory
                        .select(post.id.count())
                        .from(post)
                        .where(category.id.eq(categoryId))
                        .fetchOne())
                .orElse(0L);

        return new PageImpl<>(content, pageable, total);
    }
}