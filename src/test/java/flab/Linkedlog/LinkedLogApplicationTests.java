package flab.Linkedlog;

import flab.Linkedlog.dto.category.AddCategoryDto;
import flab.Linkedlog.dto.category.CategoryListDto;
import flab.Linkedlog.dto.category.DeleteCategoryDto;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

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
        AddCategoryDto addCategoryDto = AddCategoryDto.builder()
                .categoryName("category name")
                .build();

        // When
        Long categoryId = categoryService.addCategory(addCategoryDto);

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
        List<CategoryListDto> categories = categoryService.getAllCategory();

        // Then
        assertThat(categories).isNotEmpty();
        assertThat(categories.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
        // 카테고리 단일 삭제
    void deleteCategoryTest() {
        // Given
        Category category = categoryRepository.save(new Category("category name"));
        DeleteCategoryDto deleteCategoryDto = new DeleteCategoryDto(category.getId());

        // When
        categoryService.deleteCategory(deleteCategoryDto);

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

        List<DeleteCategoryDto> deleteCategoryDtos = List.of(
                new DeleteCategoryDto(category1.getId()),
                new DeleteCategoryDto(category2.getId())
        );

        // When
        categoryService.deleteCategories(deleteCategoryDtos);

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

        DeleteCategoryDto deleteCategoryDto = new DeleteCategoryDto(category.getId());

        // When
        categoryService.restoreCategory(deleteCategoryDto);

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

        List<DeleteCategoryDto> deleteCategoryDtos = List.of(
                new DeleteCategoryDto(category1.getId()),
                new DeleteCategoryDto(category2.getId())
        );

        // When
        categoryService.restoreCategories(deleteCategoryDtos);

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
        DeleteCategoryDto deleteCategoryDto = new DeleteCategoryDto(123L);

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> categoryService.deleteCategory(deleteCategoryDto));
    }
}
