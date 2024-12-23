package flab.Linkedlog.postTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.post.PostRepository;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class CreatePostTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Long testMemberId;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .userId("testUser")
                .password("testPassword")
                .nickName("testNickName")
                .email("testemail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .build();
        testMember = memberRepository.save(testMember);
        testMemberId = testMember.getId();

        Category testCategory = Category.builder()
                .name("Test Category")
                .build();
        categoryRepository.save(testCategory);
    }

    @AfterEach
    void tearDown() {

        postRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("글 등록 성공 테스트")
    void createPostSuccessTest() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("This is a test post content.");

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(response.getResponse()).isNotNull();

        List<Post> posts = postRepository.findAll();
        Optional<Post> savedPost = posts.stream()
                .filter(post -> "Test Post Title".equals(post.getTitle()))
                .findFirst();

        assertThat(savedPost).isPresent();
        assertThat(savedPost.get().getCategory().getId()).isEqualTo(categoryId);
        assertThat(savedPost.get().getContent()).isEqualTo("This is a test post content.");
    }

    @Test
    @DisplayName("글 등록 실패 테스트 - 인증되지 않은 사용자")
    void createPostUnauthorizedTest() throws Exception {
        // Given
        Category category = categoryRepository.save(new Category("Test Category"));
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("This is a test post content.");

        // When
        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andReturn();

        // Then
        assertThat(result.getResponse().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("글 등록 실패 테스트 - title이 비었을 때")
    void createPostTitleEmptyTest() throws Exception {
        // Given
        Category category = categoryRepository.save(new Category("Test Category"));
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("");
        createPostRequest.setContent("This is a test post content.");

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(response.getError()).contains("VALIDATION_FAILED");
    }

    @Test
    @DisplayName("글 등록 실패 테스트 - content가 비었을 때")
    void createPostContentEmptyTest() throws Exception {
        // Given
        Category category = categoryRepository.save(new Category("Test Category"));
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent(""); // 비어 있는 content

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(400);
        assertThat(response.getError()).contains("VALIDATION_FAILED");
    }
}
