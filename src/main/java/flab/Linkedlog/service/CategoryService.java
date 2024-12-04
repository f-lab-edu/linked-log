package flab.Linkedlog.service;


import flab.Linkedlog.dto.category.AddCategoryDto;
import flab.Linkedlog.dto.category.CategoryListDto;
import flab.Linkedlog.dto.category.DeleteCategoryDto;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    Logger logger = LoggerFactory.getLogger(CategoryService.class);

    // 카테고리 추가 (관리자)
    public UUID addCategory(AddCategoryDto addCategoryDto) {

        String categoryName = addCategoryDto.getCategoryName();

        Category category = new Category(categoryName);
        categoryRepository.save(category);

        return category.getId();
    }


    // 카테고리 목록 보기 (관리자)
    public List<CategoryListDto> getAllCategory() {
        return categoryRepository.findAllActive().stream()
                .map(category -> {
                    return new CategoryListDto(
                            category.getId(),
                            category.getName(),
                            category.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // 카테고리 삭제(숨김) (관리자)
    public void deleteCategory(DeleteCategoryDto deleteCategoryDto) {
        Category category = categoryRepository.findById(deleteCategoryDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        categoryRepository.eraseCategory(category);
        categoryRepository.save(category);
    }

    // 삭제 카테고리 목록 (관리자)
    public List<CategoryListDto> getAllCategoryDeleted() {
        return categoryRepository.findAllDeleted().stream()
                .map(category -> {
                    return new CategoryListDto(
                            category.getId(),
                            category.getName(),
                            category.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // 삭제 카테고리 복구 (관리자)
    public void restoreCategory(DeleteCategoryDto deleteCategoryDto) {
        Category category = categoryRepository.findById(deleteCategoryDto.getId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        categoryRepository.restoreCategory(category);
        categoryRepository.save(category);
    }

    // 다중 삭제
    public void deleteCategories(List<DeleteCategoryDto> deleteCategoryDtos) {
        List<UUID> categoryIds = deleteCategoryDtos.stream()
                .map(DeleteCategoryDto::getId)
                .toList();

        categoryRepository.batchDeleteCategory(categoryIds);
    }

    // 다중 복구
    public void restoreCategories(List<DeleteCategoryDto> deleteCategoryDtos) {
        List<UUID> categoryIds = deleteCategoryDtos.stream()
                .map(DeleteCategoryDto::getId)
                .toList();

        categoryRepository.batchRestoreCategory(categoryIds);
    }

    // 유저별 카테고리 등록

}

