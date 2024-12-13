package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CategoryListTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {

        String[] categoryNames = {"분류1", "분류2", "분류3", "분류4", "분류5", "분류6"};
        String[] deletedCategoryNames = {"분류3", "분류5", "분류6"};

        for (int i = 0; i < categoryNames.length; i++) {
            Category category = new Category(categoryNames[i]);
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

    @Test
    @DisplayName("카테고리 목록 조회 (active)")
    void getCategoriesWithActiveStatus() throws Exception {
        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        // When & Then
        mockMvc.perform(get("/admin/categoryList?status=active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.data.size()").value(3))
                .andExpect(jsonPath("$.response.data[0].categoryName").value("분류1"))
                .andExpect(jsonPath("$.response.data[1].categoryName").value("분류2"))
                .andExpect(jsonPath("$.response.data[2].categoryName").value("분류4"))
                .andExpect(jsonPath("$.response.data[0].createdAt").exists())
                .andExpect(jsonPath("$.response.data[1].createdAt").exists())
                .andExpect(jsonPath("$.response.data[2].createdAt").exists());
    }

    @Test
    @DisplayName("카테고리 목록 조회 (deleted)")
    void getCategoriesWithDeletedStatus() throws Exception {
        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        // When & Then
        mockMvc.perform(get("/admin/categoryList?status=deleted")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()) // Then: 200 OK
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response.data.size()").value(3))
                .andExpect(jsonPath("$.response.data[0].categoryName").value("분류3"))
                .andExpect(jsonPath("$.response.data[1].categoryName").value("분류5"))
                .andExpect(jsonPath("$.response.data[2].categoryName").value("분류6"))
                .andExpect(jsonPath("$.response.data[0].createdAt").exists())
                .andExpect(jsonPath("$.response.data[1].createdAt").exists())
                .andExpect(jsonPath("$.response.data[2].createdAt").exists());
    }

    @Test
    @DisplayName("유효하지 않은 status")
    void getCategoriesWithInvalidStatus() throws Exception {
        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        // When & Then
        mockMvc.perform(get("/admin/categoryList?status=invalid")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

    }
}
