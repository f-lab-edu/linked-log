package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.dto.category.CategoryListResponse;
import flab.Linkedlog.dto.category.CategoryDeleteRequest;
import flab.Linkedlog.entity.enums.CategoryStates;
import flab.Linkedlog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping(value = "/admin/addcategory")
    public ApiResponse<String> addCategory(@RequestBody @Validated CategoryCreateRequest categoryCreateRequest) {
        categoryService.addCategory(categoryCreateRequest);

        return ApiResponse.success(categoryCreateRequest.getCategoryName());

    }

    @GetMapping(value = "/admin/categoryList")
    public ApiResponse<List<CategoryListResponse>> getCategories(
            @RequestParam(name = "status", defaultValue = "ACTIVE") CategoryStates status) {
        List<CategoryListResponse> categories;

        if (status == CategoryStates.DELETED) {
            categories = categoryService.getAllCategoryDeleted();
        } else {
            categories = categoryService.getAllCategory();
        }

        return ApiResponse.success(categories);
    }

    // 카테고리 삭제/복구
    @PutMapping(value = "/admin/deletecategory")
    public ApiResponse<Void> deleteCategories(
            @RequestBody List<CategoryDeleteRequest> categoryDtos) {
        categoryService.deleteCategories(categoryDtos);

        return ApiResponse.success(null);
    }


    @PutMapping(value = "/admin/restorecategory")
    public ApiResponse<Void> restoreCategories(
            @RequestBody List<CategoryDeleteRequest> categoryDtos) {
        categoryService.restoreCategories(categoryDtos);

        return ApiResponse.success(null);
    }

}
