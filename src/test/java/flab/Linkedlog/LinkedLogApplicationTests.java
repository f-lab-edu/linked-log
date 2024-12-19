package flab.Linkedlog;

import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.dto.category.CategoryListResponse;
import flab.Linkedlog.dto.category.CategoryDeleteRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Test
        // 카테고리 추가
    void addCategoryTest() {
        // Given
        CategoryCreateRequest categoryCreateRequest = CategoryCreateRequest.builder()
                .categoryName("category name")
                .build();

        // When
        Long categoryId = categoryService.addCategory(categoryCreateRequest);

        // Then
        Category savedCategory = categoryRepository.findById(categoryId).orElseThrow();
        assertThat(savedCategory.getName()).isEqualTo("category name");
    }

    @Test
        // 카테고리 출력
    void getAllActiveCategoryTest() {
        // Given
        categoryRepository.save(new Category("category name 1"));
        categoryRepository.save(new Category("category name 2"));

        // When
        List<CategoryListResponse> categories = categoryService.getAllCategory();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
        // 카테고리 단일 삭제
    void deleteCategoryTest() {
        // Given
        Category category = categoryRepository.save(new Category("category name"));
        CategoryDeleteRequest categoryDeleteRequest = new CategoryDeleteRequest(category.getId());

        // When
        //categoryService.deleteCategory(categoryDeleteRequest);

        // Then
        Category deletedCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(deletedCategory.getDeletedAt()).isNotNull();
    }

    @Test
        // 카테고리 다중 삭제
    void deleteCategoriesTest() {
        // Given
        Category category1 = categoryRepository.save(new Category("category name 1"));
        Category category2 = categoryRepository.save(new Category("category name 2"));

        List<CategoryDeleteRequest> categoryDeleteRequests = List.of(
                new CategoryDeleteRequest(category1.getId()),
                new CategoryDeleteRequest(category2.getId())
        );

        // When
        categoryService.deleteCategories(categoryDeleteRequests);

        // Then
        List<Category> deletedCategories = categoryRepository.findAllById(
                List.of(category1.getId(), category2.getId())
        );

        assertThat(deletedCategories).allMatch(category -> category.getDeletedAt() != null);
    }

    @Test
        // 카테고리 단일 복구
    void restoreCategoryTest() {
        // Given
        Category category = categoryRepository.save(new Category("category name"));
        category.markAsDeleted();
        categoryRepository.save(category);

        CategoryDeleteRequest categoryDeleteRequest = new CategoryDeleteRequest(category.getId());

        // When
        //categoryService.restoreCategory(categoryDeleteRequest);

        // Then
        Category restoredCategory = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(restoredCategory.getDeletedAt()).isNull();
    }

    @Test
        // 카테고리 다중 복구
    void restoreCategoriesTest() {
        // Given
        Category category1 = categoryRepository.save(new Category("category name 1"));
        Category category2 = categoryRepository.save(new Category("category name 2"));

        category1.markAsDeleted();
        category2.markAsDeleted();
        categoryRepository.saveAll(List.of(category1, category2));

        List<CategoryDeleteRequest> categoryDeleteRequests = List.of(
                new CategoryDeleteRequest(category1.getId()),
                new CategoryDeleteRequest(category2.getId())
        );

        // When
        categoryService.restoreCategories(categoryDeleteRequests);

        // Then
        List<Category> restoredCategories = categoryRepository.findAllById(
                List.of(category1.getId(), category2.getId())
        );

        assertThat(restoredCategories).allMatch(category -> category.getDeletedAt() == null);
    }

    @Test
        // 삭제 시 카테고리가 존재하지 않을 경우 예외 처리
    void deleteCategoryNotExistTest() {
        // Given
        CategoryDeleteRequest categoryDeleteRequest = new CategoryDeleteRequest(123L);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory(categoryDeleteRequest));
    }
}
