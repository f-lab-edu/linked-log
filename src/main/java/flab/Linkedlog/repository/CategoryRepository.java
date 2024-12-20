package flab.Linkedlog.repository;


import flab.Linkedlog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // save
    // findById
    // delete

    // 이름으로 검색
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name%")
    List<Category> findByContainingName(@Param("name") String name);

    // 카테고리 목록
    @Query("select c from Category c where c.deletedAt is null")
    List<Category> findAllActive();

    // 숨김, 삭제 카테고리 목록
    @Query("select c from Category c where c.deletedAt is not null")
    List<Category> findAllDeleted();


    // 일괄 삭제(최적화)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.deletedAt = CURRENT_TIMESTAMP WHERE c.id IN :ids")
    void batchDeleteCategory(@Param("ids") List<Long> ids);

    // 일괄 복구(최적화)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Category c SET c.deletedAt = null WHERE c.id IN :ids")
    void batchRestoreCategory(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM Category c WHERE c.id IN :ids AND c.deletedAt IS NOT NULL")
    List<Category> findDeletedCategoriesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT c FROM Category c WHERE c.id IN :ids AND c.deletedAt IS NULL")
    List<Category> findExistingCategoriesByIds(@Param("ids") List<Long> ids);

}
