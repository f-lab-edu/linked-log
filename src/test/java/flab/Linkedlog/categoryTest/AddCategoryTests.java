package flab.Linkedlog.categoryTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.dto.category.CategoryCreateRequest;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Test
    @DisplayName("Admin 계정으로 카테고리 추가 성공")
    void addCategoryWithAdminRoleTest() throws Exception {

        // Given
        String adminToken = jwtUtil.generateToken("adminUser", MemberGrade.ADMIN);

        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("Admin Category")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/addcategory")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Then: 응답 상태가 200이어야 함
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").isNotEmpty());
    }

    @Test
    @DisplayName("General 계정으로 카테고리 추가 시 실패")
    void addCategoryWithGeneralRoleTest() throws Exception {
        // Given
        String userToken = jwtUtil.generateToken("generalUser", MemberGrade.GENERAL);
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("User Category")
                .build();

        // When & Then
        mockMvc.perform(post("/admin/addcategory")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").doesNotExist());
    }

    @Test
    @DisplayName("토큰 없이 카테고리 추가 시 실패")
    void addCategoryWithoutTokenTest() throws Exception {
        // Given
        CategoryCreateRequest request = CategoryCreateRequest.builder()
                .categoryName("Unauthorized Category")
                .build();

        // When &Then
        mockMvc.perform(post("/admin/addcategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

}
