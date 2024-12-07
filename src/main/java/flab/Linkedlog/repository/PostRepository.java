package flab.Linkedlog.repository;

import flab.Linkedlog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // save
    // findById
    // delete


    // 특정 카테고리의 글을 날짜 기준 내림차순으로 조회
    @Query("""
                SELECT p 
                FROM Post p 
                JOIN FETCH p.category c 
                JOIN FETCH p.member m 
                WHERE c.id = :categoryId 
                ORDER BY p.createdAt DESC
            """)
    public List<Post> findPostListInCategory(@Param("categoryId") Long category);


    @Query("""
            SELECT p 
            FROM Post p 
            JOIN FETCH p.category c 
            JOIN FETCH p.member m 
            WHERE c.id = :categoryId 
              AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%)
            ORDER BY p.createdAt DESC
            """)
    public List<Post> findPostListInCategoryContainKeyword(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword);

}
