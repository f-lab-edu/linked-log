package flab.Linkedlog.postTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.PostDetailResponse;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PostDetailTests {

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


    private Category testCategory;
    private Member testWriter;
    private Long testReaderId;
    private Long testWriterId;
    private Long testCategoryId;
    private Long postId;
    @Value("${profile.default-image-url}")
    private String defaultProfileImage;
    String contentExample = "가나다ㄱㄴㄷㅏㅖㅞABCabc12345＃＠※☆★/.,!?天地人あいうえおアイウエオ\uD83D\uDD22\uD83C\uDFB8\uD83D\uDCB2\uD83E\uDE99\uD83D\uDDA5\uFE0F\uD83D\uDD34⭕\uD83D\uDD20";


    @BeforeEach
    void setUp() {

        Member testReader = Member.builder()
                .userId("testReader")
                .password("testPassword")
                .nickName("readerNickName")
                .email("readeremail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        testReader = memberRepository.save(testReader);
        testReaderId = testReader.getId();

        testWriter = Member.builder()
                .userId("testWriter")
                .password("testPassword")
                .nickName("writerNickName")
                .email("writeremail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .profileImage(defaultProfileImage)
                .build();
        testWriter = memberRepository.save(testWriter);
        testWriterId = testWriter.getId();

        testCategory = Category.builder()
                .name("Test Category")
                .build();
        categoryRepository.save(testCategory);
        testCategoryId = testCategory.getId();

        Post testPost = Post.builder()
                .title("Test Post Title")
                .content(contentExample)
                .category(testCategory)
                .member(testWriter)
                .build();
        testPost = postRepository.save(testPost);
        postId = testPost.getId();

    }

    @AfterEach
    void tearDown() {

        postRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("작성 글 비회원 단건 조회 테스트(실패)")
    void readPostLogOutSuccessTest() throws Exception {

        // When & Then
        mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @DisplayName("작성 글 회원 단건 조회 테스트(성공)")
    void readPostByMemberSuccessTest() throws Exception {

        // Given
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, testReaderId);

        MvcResult result = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();


        ApiResponse<PostDetailResponse> apiResponse = objectMapper.readValue(responseContent, new TypeReference<ApiResponse<PostDetailResponse>>() {
        });
        PostDetailResponse postDetail = apiResponse.getResponse();

        assertThat(postDetail.getTitle()).isEqualTo("Test Post Title");
        assertThat(postDetail.getContent()).isEqualTo(contentExample);
        assertThat(postDetail.getCategoryId()).isEqualTo(testCategoryId);
        assertThat(postDetail.getNickname()).isEqualTo("writerNickName");
        assertThat(postDetail.getViewes()).isEqualTo(1);
        assertThat(postDetail.getPrice()).isEqualTo(new BigDecimal("0"));

    }


    @Test
    @DisplayName("다수 열람 조회수 증가 확인")
    void countPostViewsTest() throws Exception {

        // Given
        Long testReaderId1 = 100L;
        Long testReaderId2 = 200L;
        Long testReaderId3 = 300L;

        String token1 = jwtUtil.generateToken("member1", MemberGrade.GENERAL, testReaderId1);
        String token2 = jwtUtil.generateToken("member2", MemberGrade.GENERAL, testReaderId2);
        String token3 = jwtUtil.generateToken("member3", MemberGrade.GENERAL, testReaderId3);

        Random random = new Random();
        int viewsForMember1 = random.nextInt(10) + 1;
        int viewsForMember2 = random.nextInt(10) + 1;
        int viewsForMember3 = random.nextInt(10) + 1;

        MvcResult initialResult = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String initialResponseContent = initialResult.getResponse().getContentAsString();
        ApiResponse<PostDetailResponse> initialApiResponse = objectMapper.readValue(initialResponseContent, new TypeReference<>() {
        });
        PostDetailResponse initialPostDetail = initialApiResponse.getResponse();
        int initialViews = initialPostDetail.getViewes();

        // When
        for (int i = 0; i < viewsForMember1; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        for (int i = 0; i < viewsForMember2; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token2)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        for (int i = 0; i < viewsForMember3; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token3)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        MvcResult finalResult = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String finalResponseContent = finalResult.getResponse().getContentAsString();
        ApiResponse<PostDetailResponse> finalApiResponse = objectMapper.readValue(finalResponseContent, new TypeReference<>() {
        });
        PostDetailResponse finalPostDetail = finalApiResponse.getResponse();
        int finalViews = finalPostDetail.getViewes();

        // Then
        int expectedViews = initialViews + viewsForMember1 + viewsForMember2 + viewsForMember3 + 1;
        assertThat(finalViews).isEqualTo(expectedViews);
    }


    @Test
    @DisplayName("존재하지 않는 카테고리 아이디로 접근")
    void readPostFailByNotExistCategoryTest() throws Exception {

        // Given
        Long nonExistCategoryId = testCategoryId + 9999L;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, testReaderId);

        // When & Then
        mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", nonExistCategoryId, postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("존재하지 않는 포스트 아이디로 접근")
    void readPostFailByNotExistPostTest() throws Exception {

        // Given
        Long nonExistPostId = postId + 9999L;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, testReaderId);

        mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, nonExistPostId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("이미지가 포함된 글 조회 테스트")
    void readPostWithImagesSuccessTest() throws Exception {
        // Given
        int[] imageUrls = {11, 22, 13, 14, 25, 16, 27, 28, 29};

        Post testPost1 = Post.builder()
                .title("Test Post Title With Images")
                .content("content")
                .category(testCategory)
                .member(testWriter)
                .build();
        testPost1 = postRepository.save(testPost1);
        Long postId1 = testPost1.getId();

        Post testPost2 = Post.builder()
                .title("Test Post Title With Images")
                .content("content")
                .category(testCategory)
                .member(testWriter)
                .build();
        testPost2 = postRepository.save(testPost2);
        Long postId2 = testPost2.getId();

        List<PostImage> postImages = new ArrayList<>();
        for (int i = 0; i < imageUrls.length; i++) {
            Post post = (imageUrls[i] < 20) ? testPost1 : testPost2;
            String imageUrl = "imageUrls" + imageUrls[i];

            PostImage postImage = PostImage.builder()
                    .post(post)
                    .imageUrl(imageUrl)
                    .build();
            postImages.add(postImage);
        }

        postImageRepository.saveAll(postImages);

        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, testReaderId);

        // When
        MvcResult result1 = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId1)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult result2 = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId2)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String responseContent1 = result1.getResponse().getContentAsString();
        ApiResponse<PostDetailResponse> apiResponse1 = objectMapper.readValue(responseContent1, new TypeReference<>() {
        });
        PostDetailResponse postDetail1 = apiResponse1.getResponse();

        String responseContent2 = result2.getResponse().getContentAsString();
        ApiResponse<PostDetailResponse> apiResponse2 = objectMapper.readValue(responseContent2, new TypeReference<>() {
        });
        PostDetailResponse postDetail2 = apiResponse2.getResponse();


        assertThat(postDetail1.getImages()).hasSize(4);
        List<String> expectedPost1Urls = List.of("imageUrls11", "imageUrls13", "imageUrls14", "imageUrls16");
        for (int i = 0; i < postDetail1.getImages().size(); i++) {
            assertThat(postDetail1.getImages().get(i)).isEqualTo(expectedPost1Urls.get(i));
        }

        assertThat(postDetail2.getImages()).hasSize(5);
        List<String> expectedPost2Urls = List.of("imageUrls22", "imageUrls25", "imageUrls27", "imageUrls28", "imageUrls29");
        for (int i = 0; i < postDetail2.getImages().size(); i++) {
            assertThat(postDetail2.getImages().get(i)).isEqualTo(expectedPost2Urls.get(i));
        }

        postImageRepository.deleteAll();

    }

}
