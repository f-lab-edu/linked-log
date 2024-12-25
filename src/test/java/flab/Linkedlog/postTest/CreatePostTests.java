package flab.Linkedlog.postTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.entity.Category;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.PostImage;
import flab.Linkedlog.entity.enums.MemberGrade;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.post.PostRepository;
import flab.Linkedlog.repository.postImage.PostImageRepository;
import flab.Linkedlog.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;


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

    @Autowired
    private PostImageRepository postImageRepository;

    private Long testMemberId;
    @Value("${profile.default-image-url}")
    private String defaultProfileImage;

    @BeforeEach
    void setUp() {
        Member testMember = Member.builder()
                .userId("testUser")
                .password("testPassword")
                .nickName("testNickName")
                .email("testemail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
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
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);

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

        // When & Then
        mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isUnauthorized());
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

        // When & Then
        mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isBadRequest());
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

        // When & Then
        mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createPostRequest)))
                .andExpect(status().isBadRequest());
    }

//
//    @Test
//    @DisplayName("이미지 1개 업로드 테스트")
//    void createPostWithImageTest() throws Exception {
//        // Given
//        Category category = categoryRepository.findAll().get(0);
//        Long categoryId = category.getId();
//
//        CreatePostRequest createPostRequest = new CreatePostRequest();
//        createPostRequest.setTitle("Test Post Title");
//        createPostRequest.setContent("This is a test post content.");
//
//        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);
//
//        // 이미지 파일 생성 (mock 파일 생성)
//        MockMultipartFile imageFile = new MockMultipartFile(
//                "images",
//                "test-image-one.jpg",
//                "image/jpeg",
//                "dummy image content".getBytes());
//
//        // When
//        MvcResult result = mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
//                        .file(imageFile)
//                        .header("Authorization", "Bearer " + token)
//                        .param("title", createPostRequest.getTitle())
//                        .param("content", createPostRequest.getContent())
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andReturn();
//
//        // Then
//        String jsonResponse = result.getResponse().getContentAsString();
//        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);
//
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//        assertThat(response.getResponse()).isNotNull();
//
//        List<Post> posts = postRepository.findAll();
//        Optional<Post> savedPost = posts.stream()
//                .filter(post -> "Test Post Title".equals(post.getTitle()))
//                .findFirst();
//
//        assertThat(savedPost).isPresent();
//        assertThat(savedPost.get().getCategory().getId()).isEqualTo(categoryId);
//        assertThat(savedPost.get().getContent()).isEqualTo("This is a test post content.");
//
//        List<PostImage> postImages = postImageRepository.findAllByPostId(savedPost.get());
//        assertThat(postImages).isNotEmpty();  // 이미지가 비어있지 않아야 함
//        assertThat(postImages.size()).isEqualTo(1);  // 이미지가 1개여야 함
//        assertThat(postImages.get(0).getImageUrl()).contains("test-image.jpg");  // 저장된 이미지 URL 확인
//    }
//
//
//    @Test
//    @DisplayName("이미지 다수 업로드 테스트")
//    void createPostSeveralImageTest() throws Exception {
//        // Given
//        Category category = categoryRepository.findAll().get(0);
//        Long categoryId = category.getId();
//
//        CreatePostRequest createPostRequest = new CreatePostRequest();
//        createPostRequest.setTitle("Test Post Title");
//        createPostRequest.setContent("This is a test post content.");
//
//        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);
//
//        // When
//        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createPostRequest)))
//                .andReturn();
//
//        // Then
//        String jsonResponse = result.getResponse().getContentAsString();
//        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);
//
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//        assertThat(response.getResponse()).isNotNull();
//
//        List<Post> posts = postRepository.findAll();
//        Optional<Post> savedPost = posts.stream()
//                .filter(post -> "Test Post Title".equals(post.getTitle()))
//                .findFirst();
//
//        assertThat(savedPost).isPresent();
//        assertThat(savedPost.get().getCategory().getId()).isEqualTo(categoryId);
//        assertThat(savedPost.get().getContent()).isEqualTo("This is a test post content.");
//    }
//
//
//    @Test
//    @DisplayName("이미지 개수 초과 테스트")
//    void createPostExcessImageTest() throws Exception {
//        // Given
//        Category category = categoryRepository.findAll().get(0);
//        Long categoryId = category.getId();
//
//        CreatePostRequest createPostRequest = new CreatePostRequest();
//        createPostRequest.setTitle("Test Post Title");
//        createPostRequest.setContent("This is a test post content.");
//
//        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);
//
//        // When
//        MvcResult result = mockMvc.perform(post("/posts/category/{categoryId}/write", categoryId)
//                        .header("Authorization", "Bearer " + token)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(createPostRequest)))
//                .andReturn();
//
//        // Then
//        String jsonResponse = result.getResponse().getContentAsString();
//        ApiResponse response = objectMapper.readValue(jsonResponse, ApiResponse.class);
//
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//        assertThat(response.getResponse()).isNotNull();
//
//        List<Post> posts = postRepository.findAll();
//        Optional<Post> savedPost = posts.stream()
//                .filter(post -> "Test Post Title".equals(post.getTitle()))
//                .findFirst();
//
//        assertThat(savedPost).isPresent();
//        assertThat(savedPost.get().getCategory().getId()).isEqualTo(categoryId);
//        assertThat(savedPost.get().getContent()).isEqualTo("This is a test post content.");
//    }

}
