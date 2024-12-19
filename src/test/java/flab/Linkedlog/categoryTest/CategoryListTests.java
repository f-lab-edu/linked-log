package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.category.CategoryListResponse;
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
import static org.assertj.core.api.Assertions.assertThat;

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

        // When
        String responseContent = mockMvc.perform(get("/admin/categoryList?status=ACTIVE")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("Response Content: " + responseContent);

        ApiResponse<List<CategoryListResponse>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).hasSize(3);
        assertThat(response.getResponse().get(0).getCategoryName()).isEqualTo("분류1");
        assertThat(response.getResponse().get(1).getCategoryName()).isEqualTo("분류2");
        assertThat(response.getResponse().get(2).getCategoryName()).isEqualTo("분류4");
        assertThat(response.getResponse().get(0).getCreatedAt()).isNotNull();
        assertThat(response.getResponse().get(1).getCreatedAt()).isNotNull();
        assertThat(response.getResponse().get(2).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("카테고리 목록 조회 (deleted)")
    void getCategoriesWithDeletedStatus() throws Exception {
        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        // When
        String responseContent = mockMvc.perform(get("/admin/categoryList?status=DELETED")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<List<CategoryListResponse>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });

        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getResponse()).hasSize(3);
        assertThat(response.getResponse().get(0).getCategoryName()).isEqualTo("분류3");
        assertThat(response.getResponse().get(1).getCategoryName()).isEqualTo("분류5");
        assertThat(response.getResponse().get(2).getCategoryName()).isEqualTo("분류6");
        assertThat(response.getResponse().get(0).getCreatedAt()).isNotNull();
        assertThat(response.getResponse().get(1).getCreatedAt()).isNotNull();
        assertThat(response.getResponse().get(2).getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("유효하지 않은 status")
    void getCategoriesWithInvalidStatus() throws Exception {
        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        // When
        String responseContent = mockMvc.perform(get("/admin/categoryList?status=invalid")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<?> response = objectMapper.readValue(responseContent, ApiResponse.class);

        // Then
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).isEqualTo("INVALID_STATUS");
    }
}
