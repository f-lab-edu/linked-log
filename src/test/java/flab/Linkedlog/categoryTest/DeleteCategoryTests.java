package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        String[] categoryNames = {"분류1", "분류2", "분류3", "분류4", "분류5", "분류6", "분류7", "분류8", "분류9", "분류10"};
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
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
    }

    // 1개 삭제
    @Test
    @DisplayName("1개 카테고리 삭제")
    void deleteSingleCategory() throws Exception {

        List<Category> categories = categoryRepository.findByContainingName("분류2");
        Long categoryId = categories.get(0).getId();

        mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Category category = categoryRepository.findById(categoryId).orElseThrow();
        assert category.getDeletedAt() != null;
    }

    // 1개 복구
    @Test
    @DisplayName("1개 카테고리 복구 (이미 삭제된 카테고리)")
    void restoreSingleCategory() throws Exception {

        List<Category> categories = categoryRepository.findByContainingName("분류1");
        Long categoryId = categories.get(0).getId();

        mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Category restoredCategory = categoryRepository.findById(categoryId).orElseThrow();
        assert restoredCategory.getDeletedAt() == null;
    }

    // 다중 삭제
    @Test
    @DisplayName("다중 카테고리 삭제")
    void deleteMultipleCategories() throws Exception {
        // 삭제할 카테고리 ID 목록
        List<Category> categories = categoryRepository.findByContainingName("분류2");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류4");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류8");
        Long categoryId3 = categories.get(0).getId();

        mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(
                                new CategoryDeleteRequest(categoryId1),
                                new CategoryDeleteRequest(categoryId2),
                                new CategoryDeleteRequest(categoryId3)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assert categoryRepository.findById(categoryId1).get().getDeletedAt() != null;
        assert categoryRepository.findById(categoryId2).get().getDeletedAt() != null;
        assert categoryRepository.findById(categoryId3).get().getDeletedAt() != null;
    }

    // 다중 복구
    @Test
    @DisplayName("다중 카테고리 복구")
    void restoreMultipleCategories() throws Exception {

        List<Category> categories = categoryRepository.findByContainingName("분류3");
        Long categoryId1 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류5");
        Long categoryId2 = categories.get(0).getId();
        categories = categoryRepository.findByContainingName("분류9");
        Long categoryId3 = categories.get(0).getId();

        mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(
                                new CategoryDeleteRequest(categoryId1),
                                new CategoryDeleteRequest(categoryId2),
                                new CategoryDeleteRequest(categoryId3)
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assert categoryRepository.findById(categoryId1).get().getDeletedAt() == null;
        assert categoryRepository.findById(categoryId2).get().getDeletedAt() == null;
        assert categoryRepository.findById(categoryId3).get().getDeletedAt() == null;
    }

    /*
    // 0개 선택 삭제
    @Test
    @DisplayName("0개 선택 삭제")
    void deleteZeroCategories() throws Exception {
        mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        long initialCount = categoryRepository.count();
        assert initialCount == categoryRepository.count();
    }

    // 0개 선택 복구
    @Test
    @DisplayName("0개 선택 복구")
    void restoreZeroCategories() throws Exception {
        mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        long initialCount = categoryRepository.count();
        assert initialCount == categoryRepository.count();
    }

    // 이미 삭제된 카테고리 삭제
    @Test
    @DisplayName("이미 삭제된 카테고리 삭제")
    void deleteAlreadyDeletedCategory() throws Exception {
        List<Category> categories = categoryRepository.findByContainingName("분류1");
        Long categoryId = categories.get(0).getId();

        mockMvc.perform(put("/admin/deletecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // 이미 복구된 카테고리 복구
    @Test
    @DisplayName("이미 복구된 카테고리 복구")
    void restoreAlreadyRestoredCategory() throws Exception {
        List<Category> categories = categoryRepository.findByContainingName("분류2");
        Long categoryId = categories.get(0).getId();

        mockMvc.perform(put("/admin/restorecategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(List.of(new CategoryDeleteRequest(categoryId)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
*/

}
