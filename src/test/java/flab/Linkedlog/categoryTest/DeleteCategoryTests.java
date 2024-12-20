package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.category.CategoryDeleteRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class DeleteCategoryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {

        adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        String[] categoryNames = {"분류0", "분류1", "분류2", "분류3", "분류4", "분류5", "분류6", "분류7", "분류8", "분류9"};
        String[] deletedCategoryNames = {"분류1", "분류3", "분류5", "분류7", "분류9"};  // 삭제할 카테고리 이름 배열

        for (String categoryName : categoryNames) {
            Category category = new Category(categoryName);
            categoryRepository.save(category);
        }

        for (String deletedCategoryName : deletedCategoryNames) {
            List<Category> categoriesToDelete = categoryRepository.findByContainingName(deletedCategoryName);
            categoriesToDelete.forEach(category -> {
                category.markAsDeleted();
                categoryRepository.save(category);
            });
        }

        for (String categoryName : categoryNames) {
            List<Category> categories = categoryRepository.findByContainingName(categoryName);
            for (Category category : categories) {
                System.out.println("카테고리 이름: " + category.getName() + " | DeletedAt: " + category.getDeletedAt());
            }
        }
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    // 1개 삭제
    @Test
    @DisplayName("1개 카테고리 삭제")
    void deleteSingleCategory() throws Exception {
        //Given
        List<Category> categories = categoryRepository.findByContainingName("분류0");
        Long categoryId = categories.get(0).getId();

        // When
        String responseContent = mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();

        Category category = categoryRepository.findById(categoryId).orElseThrow();
        assertThat(category.getDeletedAt()).isNotNull();
    }


    @Test
    @DisplayName("1개 카테고리 복구")
    void restoreSingleCategory() throws Exception {
        // Given
        List<Category> categories = categoryRepository.findByContainingName("분류1");
        Long categoryId = categories.get(0).getId();

        // When
        String responseContent = mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();

        Category category = categoryRepository.findById(categoryId).orElseThrow();
        assertThat(category.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("다중 카테고리 삭제")
    void deleteMultipleCategories() throws Exception {
        // Given
        List<Category> categories = categoryRepository.findByContainingName("분류2");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류4");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류8");
        Long categoryId3 = categories.get(0).getId();

        List<CategoryDeleteRequest> deleteRequests = List.of(
                new CategoryDeleteRequest(categoryId1),
                new CategoryDeleteRequest(categoryId2),
                new CategoryDeleteRequest(categoryId3)
        );

        // When
        String responseContent = mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deleteRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();

        assertThat(categoryRepository.findById(categoryId1))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNotNull();

        assertThat(categoryRepository.findById(categoryId2))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNotNull();

        assertThat(categoryRepository.findById(categoryId3))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNotNull();
    }

    @Test
    @DisplayName("다중 카테고리 복구")
    void restoreMultipleCategories() throws Exception {
        // Given
        List<Category> categories = categoryRepository.findByContainingName("분류3");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류5");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류9");
        Long categoryId3 = categories.get(0).getId();

        List<CategoryDeleteRequest> restoreRequests = List.of(
                new CategoryDeleteRequest(categoryId1),
                new CategoryDeleteRequest(categoryId2),
                new CategoryDeleteRequest(categoryId3)
        );

        // When
        String responseContent = mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(restoreRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();

        assertThat(categoryRepository.findById(categoryId1))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNull();

        assertThat(categoryRepository.findById(categoryId2))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNull();

        assertThat(categoryRepository.findById(categoryId3))
                .isPresent()
                .get()
                .extracting(Category::getDeletedAt)
                .isNull();


    }


    @Test
    @DisplayName("0개 선택 삭제")
    void deleteZeroCategories() throws Exception {

        // Given
        List<CategoryDeleteRequest> emptyCategoryDeleteRequests = List.of();

        // When
        String responseContent = mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(emptyCategoryDeleteRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();
    }

    // 0개 선택 복구
    @Test
    @DisplayName("0개 선택 복구")
    void restoreZeroCategories() throws Exception {
        // Given
        List<CategoryDeleteRequest> emptyCategoryDeleteRequests = List.of();

        // When
        String responseContent = mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(emptyCategoryDeleteRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Then
        ApiResponse<Void> apiResponse = objectMapper.readValue(responseContent, new TypeReference<>() {
        });

        assertThat(apiResponse.getError()).isNull();
    }


    @Test
    @DisplayName("이미 삭제된 카테고리 삭제(실패)")
    void deleteAlreadyDeletedCategory() throws Exception {
        // Given
        List<Category> categories = categoryRepository.findByContainingName("분류9");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류3");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류0");
        Long categoryId3 = categories.get(0).getId();

        List<CategoryDeleteRequest> deleteRequests = List.of(
                new CategoryDeleteRequest(categoryId1),
                new CategoryDeleteRequest(categoryId2),
                new CategoryDeleteRequest(categoryId3)
        );

        // When
        String responseContent = mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deleteRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        // Then
        assertThat(response.getError()).isEqualTo("INVALID_STATE");  // 예외 메시지 확인
    }


    @Test
    @DisplayName("이미 존재하는 카테고리 복구(실패)")
    void restoreAlreadyRestoredCategory() throws Exception {
        // Given
        List<Category> categories = categoryRepository.findByContainingName("분류2");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류6");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류1");
        Long categoryId3 = categories.get(0).getId();

        List<CategoryDeleteRequest> deleteRequests = List.of(
                new CategoryDeleteRequest(categoryId1),
                new CategoryDeleteRequest(categoryId2),
                new CategoryDeleteRequest(categoryId3)
        );

        // When
        String responseContent = mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(deleteRequests)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        // Then
        assertThat(response.getError()).isEqualTo("INVALID_STATE");  // 예외 메시지 확인
    }


}
