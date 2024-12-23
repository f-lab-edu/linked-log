package flab.Linkedlog.service;


import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.dto.category.CategoryListResponse;
import flab.Linkedlog.dto.category.CategoryDeleteRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 카테고리 추가 (관리자)
    public Long addCategory(CategoryCreateRequest categoryCreateRequest) {

        String categoryName = categoryCreateRequest.getCategoryName();

        Category category = new Category(categoryName);
        categoryRepository.save(category);

        return category.getId();
    }


    // 카테고리 목록 보기 (관리자)
    public List<CategoryListResponse> getAllCategory() {
        return categoryRepository.findAllActive().stream()
                .map(category -> {
                    return new CategoryListResponse(
                            category.getId(),
                            category.getName(),
                            category.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // 삭제 카테고리 목록 (관리자)
    public List<CategoryListResponse> getAllCategoryDeleted() {
        return categoryRepository.findAllDeleted().stream()
                .map(category -> {
                    return new CategoryListResponse(
                            category.getId(),
                            category.getName(),
                            category.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }


    // 카테고리 삭제(숨김) (관리자)
    public void deleteCategory(CategoryDeleteRequest categoryDeleteRequest) {
        Category category = categoryRepository.findById(categoryDeleteRequest.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (category.getDeletedAt() != null) {
            throw new IllegalStateException("이미 삭제된 카테고리입니다: " + category.getId());
        }
    }


    // 삭제 카테고리 복구 (관리자)
    public void restoreCategory(CategoryDeleteRequest categoryDeleteRequest) {
        Category category = categoryRepository.findById(categoryDeleteRequest.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        if (category.getDeletedAt() == null) {
            throw new IllegalStateException("삭제되지 않은 카테고리입니다: " + category.getId());
        }
    }

    // 다중 삭제
    public void deleteCategories(List<CategoryDeleteRequest> categoryDeleteRequests) {
        List<Long> categoryIds = categoryDeleteRequests.stream()
                .map(CategoryDeleteRequest::getId)
                .toList();

        List<Category> alreadyDeletedCategories = categoryRepository.findDeletedCategoriesByIds(categoryIds);

        if (!alreadyDeletedCategories.isEmpty()) {
            throw new IllegalStateException("이미 삭제된 카테고리가 있습니다.");
        }

        categoryRepository.batchDeleteCategory(categoryIds);
    }

    // 다중 복구
    public void restoreCategories(List<CategoryDeleteRequest> categoryDeleteRequests) {
        List<Long> categoryIds = categoryDeleteRequests.stream()
                .map(CategoryDeleteRequest::getId)
                .toList();

        List<Category> alreadyExistingCategories = categoryRepository.findExistingCategoriesByIds(categoryIds);

        if (!alreadyExistingCategories.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 카테고리입니다.");
        }

        categoryRepository.batchRestoreCategory(categoryIds);
    }

    // 유저별 카테고리 등록

}

