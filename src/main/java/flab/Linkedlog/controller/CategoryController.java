package flab.Linkedlog.controller;

import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.controller.response.ErrorResponse;
import flab.Linkedlog.controller.response.SuccessResponse;
import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.dto.category.CategoryListResponse;
import flab.Linkedlog.dto.category.CategoryDeleteRequest;
import flab.Linkedlog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping(value = "/admin/addcategory")
    public ResponseEntity<ApiResponse<?>> addCategory(@RequestBody @Validated CategoryCreateRequest categoryCreateRequest) {

        try {
            categoryService.addCategory(categoryCreateRequest);

            SuccessResponse<String> successResponse = new SuccessResponse<>(categoryCreateRequest.getCategoryName());
            ApiResponse<SuccessResponse<String>> apiResponse = ApiResponse.<SuccessResponse<String>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_ADD_FAILED", "Failed to add category");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    @GetMapping(value = "/admin/categoryList")
    public ResponseEntity<ApiResponse<?>> getCategories(
            @RequestParam(name = "status", defaultValue = "active") String status) {
        try {
            List<CategoryListResponse> categories;

            if (!"active".equals(status) && !"deleted".equals(status)) {
                ErrorResponse errorResponse = new ErrorResponse("INVALID_STATUS", "Invalid status parameter. Valid values are 'active' or 'deleted'.");
                ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                        .success(false)
                        .response(errorResponse)
                        .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
            }

            if ("deleted".equals(status)) {
                categories = categoryService.getAllCategoryDeleted();
            } else {
                categories = categoryService.getAllCategory();
            }

            SuccessResponse<List<CategoryListResponse>> successResponse = new SuccessResponse<>(categories);
            ApiResponse<SuccessResponse<List<CategoryListResponse>>> apiResponse = ApiResponse.<SuccessResponse<List<CategoryListResponse>>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();

            return ResponseEntity.ok(apiResponse);

        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_LIST_FETCH_FAILED", "Failed to fetch category list");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }

    // 카테고리 삭제/복구
    @PutMapping(value = "/admin/deletecategory")
    public ResponseEntity<ApiResponse<?>> deleteCategories(
            @RequestBody List<CategoryDeleteRequest> categoryDtos) {
        try {
            categoryService.deleteCategories(categoryDtos);
            SuccessResponse<Void> successResponse = new SuccessResponse<>(null);
            ApiResponse<SuccessResponse<Void>> apiResponse = ApiResponse.<SuccessResponse<Void>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_ALREADY_DELETED", e.getMessage());
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);


        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_DELETE_FAILED", "Failed to delete categories");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


    @PutMapping(value = "/admin/restorecategory")
    public ResponseEntity<ApiResponse<?>> modifyCategories(
            @RequestBody List<CategoryDeleteRequest> categoryDtos) {
        try {
            categoryService.restoreCategories(categoryDtos);
            SuccessResponse<Void> successResponse = new SuccessResponse<>(null);
            ApiResponse<SuccessResponse<Void>> apiResponse = ApiResponse.<SuccessResponse<Void>>builder()
                    .success(true)
                    .response(successResponse)
                    .build();
            return ResponseEntity.ok(apiResponse);

        } catch (IllegalStateException e) {
            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_ALREADY_EXISTS", e.getMessage());
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

        } catch (Exception e) {

            ErrorResponse errorResponse = new ErrorResponse("CATEGORY_RESTORE_FAILED", "Failed to restore categories");
            ApiResponse<ErrorResponse> apiResponse = ApiResponse.<ErrorResponse>builder()
                    .success(false)
                    .response(errorResponse)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
    }


}
