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
import flab.Linkedlog.service.S3Service;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private S3Service s3Service;

    private Long testMemberId;

    @Value("${aws.s3.bucket}")
    private String bucketName;

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

        postImageRepository.deleteAll();
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
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        // Mock multipart files
        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
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
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        // Mock multipart file for `createPostRequest`
        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
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
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        // Mock multipart file for `createPostRequest`
        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When & Then
        mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
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
        createPostRequest.setContent("");
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When & Then
        mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("이미지 1개 업로드 테스트")
    void createPostWithImageTest() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("This is a test post content.");
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",
                "test-image-one.jpg",
                "image/jpeg",
                "dummy image content".getBytes()
        );

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .file(imageFile)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
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

        List<PostImage> postImages = postImageRepository.findAllByPostId(savedPost.get().getId());
        assertThat(postImages).isNotEmpty();
        assertThat(postImages.size()).isEqualTo(1);
        assertThat(postImages.get(0).getImageUrl()).contains("test-image-one.jpg");

        String uploadedImageUrl = postImages.get(0).getImageUrl();

        String s3Key = uploadedImageUrl.substring(uploadedImageUrl.lastIndexOf("/") + 1);
        String s3Url = s3Service.getFileUrl(s3Key, bucketName);

        assertThat(s3Url).contains(s3Key);

        s3Service.deleteFile(s3Key, bucketName);

    }


    @Test
    @DisplayName("이미지 다수 업로드 테스트")
    void createPostMultipleImageTest() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("This is a test post content.");
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        int numberOfImages = 5;

        List<MockMultipartFile> imageFiles = new ArrayList<>();
        for (int i = 1; i <= numberOfImages; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images",
                    "test-image-" + i + ".jpg",
                    "image/jpeg",
                    ("dummy image content " + i).getBytes()
            );
            imageFiles.add(imageFile);
        }

        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .file(imageFiles.get(0))
                        .file(imageFiles.get(1))
                        .file(imageFiles.get(2))
                        .file(imageFiles.get(3))
                        .file(imageFiles.get(4))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
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

        List<PostImage> postImages = postImageRepository.findAllByPostId(savedPost.get().getId());
        assertThat(postImages).isNotEmpty();
        assertThat(postImages.size()).isEqualTo(numberOfImages);

        for (int i = 0; i < numberOfImages; i++) {
            String uploadedImageUrl = postImages.get(i).getImageUrl();
            String s3Key = uploadedImageUrl.substring(uploadedImageUrl.lastIndexOf("/") + 1);
            String s3Url = s3Service.getFileUrl(s3Key, bucketName);
            assertThat(s3Url).contains(s3Key);

            s3Service.deleteFile(s3Key, bucketName);
        }
    }

    @Test
    @DisplayName("이미지 개수 초과 테스트")
    void createPostExcessImageTest() throws Exception {
        // Given
        Category category = categoryRepository.findAll().get(0);
        Long categoryId = category.getId();

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Post Title");
        createPostRequest.setContent("This is a test post content.");
        String createPostRequestJson = objectMapper.writeValueAsString(createPostRequest);

        MockMultipartFile createPostRequestPart = new MockMultipartFile(
                "createPostRequest",
                "createPostRequest.json",
                "application/json",
                createPostRequestJson.getBytes()
        );

        int numberOfImages = 21;

        List<MockMultipartFile> imageFiles = new ArrayList<>();
        for (int i = 1; i <= numberOfImages; i++) {
            MockMultipartFile imageFile = new MockMultipartFile(
                    "images",
                    "test-image-" + i + ".jpg",
                    "image/jpeg",
                    ("dummy image content " + i).getBytes()
            );
            imageFiles.add(imageFile);
        }
        String token = jwtUtil.generateToken("testUser", MemberGrade.GENERAL, testMemberId);

        // When
        MvcResult result = mockMvc.perform(multipart("/posts/category/{categoryId}/write", categoryId)
                        .file(createPostRequestPart)
                        .file(imageFiles.get(0))
                        .file(imageFiles.get(1))
                        .file(imageFiles.get(2))
                        .file(imageFiles.get(3))
                        .file(imageFiles.get(4))
                        .file(imageFiles.get(5))
                        .file(imageFiles.get(6))
                        .file(imageFiles.get(7))
                        .file(imageFiles.get(8))
                        .file(imageFiles.get(9))
                        .file(imageFiles.get(10))
                        .file(imageFiles.get(11))
                        .file(imageFiles.get(12))
                        .file(imageFiles.get(13))
                        .file(imageFiles.get(14))
                        .file(imageFiles.get(15))
                        .file(imageFiles.get(16))
                        .file(imageFiles.get(17))
                        .file(imageFiles.get(18))
                        .file(imageFiles.get(19))
                        .file(imageFiles.get(20))
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isBadRequest())
                .andReturn();


    }
}
