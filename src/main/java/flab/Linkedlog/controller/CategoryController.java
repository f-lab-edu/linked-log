package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.category.AddCategoryDto;
import flab.Linkedlog.dto.category.CategoryListDto;
import flab.Linkedlog.dto.category.DeleteCategoryDto;
import flab.Linkedlog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping(value = "/admin-v1/addcategory")
    public ResponseEntity<ApiResponse<String>> addCategory(@RequestBody @Validated AddCategoryDto addCategoryDto) {
        categoryService.addCategory(addCategoryDto);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Add category successful")
                        .data(addCategoryDto.getCategoryName())
                        .build()
        );
    }

    @GetMapping(value = "/admin-v1/categoryList")
    public ResponseEntity<ApiResponse<List<CategoryListDto>>> getCategories(
            @RequestParam(name = "status", defaultValue = "active") String status) {
        List<CategoryListDto> categories;
        if ("deleted".equals(status)) {
            categories = categoryService.getAllCategoryDeleted();
        } else {
            categories = categoryService.getAllCategory();
        }
        return ResponseEntity.ok(
                ApiResponse.<List<CategoryListDto>>builder()
                        .success(true)
                        .message("Category list fetched successfully")
                        .data(categories)
                        .build()
        );
    }

    // 카테고리 삭제/복구
    @PutMapping(value = "/admin-v1/deletecategory")
    public ResponseEntity<ApiResponse<Void>> deleteCategories(
            @RequestBody List<DeleteCategoryDto> categoryDtos) {
        categoryService.deleteCategories(categoryDtos);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Categories deleted successfully")
                        .build()
        );
    }

    @PutMapping(value = "/admin-v1/restorecategory")
    public ResponseEntity<ApiResponse<Void>> modifyCategories(
            @RequestBody List<DeleteCategoryDto> categoryDtos) {
        categoryService.restoreCategories(categoryDtos);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Categories restored successfully")
                        .build()
        );
    }


}
