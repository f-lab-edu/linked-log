package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AddCategoryTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Admin 계정으로 카테고리 추가 성공")
    void addCategoryWithAdminRoleTest() throws Exception {

        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("Admin Category")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/admin/addcategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(response.getResponse()).isNotNull();

        List<Category> categories = categoryRepository.findByContainingName("Admin Category");
        Long categoryId = categories.get(0).getId();

        Category createdCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리가 존재하지 않습니다"));

        assertThat(createdCategory.getName()).isEqualTo("Admin Category");
    }

    @Test
    @DisplayName("General 계정으로 카테고리 추가 시 실패")
    void addCategoryWithGeneralRoleTest() throws Exception {
        // Given
        String userToken = jwtUtil.generateToken("generalUser", MemberGrade.GENERAL);
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("User Category")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/admin/addcategory")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(403);
        assertThat(result.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    @DisplayName("토큰 없이 카테고리 추가 시 실패")
    void addCategoryWithoutTokenTest() throws Exception {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("Unauthorized Category")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/admin/addcategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<?> response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(401);
        assertThat(response.getError()).isEqualTo("Unauthorized");

    }

}
