package flab.Linkedlog.postTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.PostDetailDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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

    private Long testReaderId;
    private Long testWriterId;
    private Long testCategoryId;
    private Long postId;

    @BeforeEach
    void setUp() {

        String contentExample = "가나다ㄱㄴㄷㅏㅖㅞABCabc12345＃＠※☆★/.,!?天地人あいうえおアイウエオ\uD83D\uDD22\uD83C\uDFB8\uD83D\uDCB2\uD83E\uDE99\uD83D\uDDA5\uFE0F\uD83D\uDD34⭕\uD83D\uDD20";
        // 테스트에서 사용할 멤버 생성
        Member testReader = Member.builder()
                .userId("testReader")
                .password("testPassword")
                .nickName("readerNickName")
                .email("readeremail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .build();
        testReader = memberRepository.save(testReader); // 저장 후 ID를 가져옴
        testReaderId = testReader.getId(); // 자동 생성된 ID를 저장

        Member testWriter = Member.builder()
                .userId("testWriter")
                .password("testPassword")
                .nickName("writerNickName")
                .email("writeremail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .build();
        testWriter = memberRepository.save(testWriter); // 저장 후 ID를 가져옴
        testWriterId = testWriter.getId(); // 자동 생성된 ID를 저장

        // 테스트에서 사용할 카테고리 생성
        Category testCategory = Category.builder()
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

        // When: 토큰 인증 없이 조회 요청
        var result = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then: 응답 검증
        String jsonResponse = result.getResponse().getContentAsString();
        ApiResponse<PostDetailDto> response = objectMapper.readValue(jsonResponse, ApiResponse.class);

        assertThat(result.getResponse().getStatus()).isEqualTo(401); // Bad Request
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError()).contains("Unauthorized");

    }

    @Test
    @DisplayName("작성 글 회원 단건 조회 테스트(성공)")
    void readPostByMemberSuccessTest() throws Exception {

        String contentExample = "가나다ㄱㄴㄷㅏㅖㅞABCabc12345＃＠※☆★/.,!?天地人あいうえおアイウエオ\uD83D\uDD22\uD83C\uDFB8\uD83D\uDCB2\uD83E\uDE99\uD83D\uDDA5\uFE0F\uD83D\uDD34⭕\uD83D\uDD20";

        // Given: 회원 토큰 생성
        String token = jwtUtil.generateToken("testWriter", MemberGrade.GENERAL, postId);

        // When: 토큰 인증 후 조회 요청
        MvcResult result = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String responseContent = result.getResponse().getContentAsString();


        ApiResponse<PostDetailDto> apiResponse = objectMapper.readValue(responseContent, new TypeReference<ApiResponse<PostDetailDto>>() {
        });
        PostDetailDto postDetail = apiResponse.getResponse();

        assertThat(result.getResponse().getStatus()).isEqualTo(200); // 성공 상태
        assertThat(apiResponse.isSuccess()).isTrue();
        assertThat(postDetail.getTitle()).isEqualTo("Test Post Title");
        assertThat(postDetail.getContent()).isEqualTo(contentExample);
        assertThat(postDetail.getCategoryId()).isEqualTo(testCategoryId);
        assertThat(postDetail.getNickname()).isEqualTo("writerNickName");
        assertThat(postDetail.getViewes()).isEqualTo(1);
        assertThat(postDetail.getPrice()).isEqualTo(new BigDecimal("0"));
        // 토큰 인증 후 조회
    }


    @Test
    @DisplayName("다수 열람 조회수 증가 확인")
    void countPostViewsTest() throws Exception {

        // Given: 회원 토큰 3명 생성
        String token1 = jwtUtil.generateToken("member1", MemberGrade.GENERAL, postId);
        String token2 = jwtUtil.generateToken("member2", MemberGrade.GENERAL, postId);
        String token3 = jwtUtil.generateToken("member3", MemberGrade.GENERAL, postId);

        // 랜덤 조회수 생성 (1~10 사이의 랜덤 숫자)
        Random random = new Random();
        int viewsForMember1 = random.nextInt(10) + 1; // 1~10 랜덤 숫자
        int viewsForMember2 = random.nextInt(10) + 1; // 1~10 랜덤 숫자
        int viewsForMember3 = random.nextInt(10) + 1; // 1~10 랜덤 숫자

        // 1. 게시글의 초기 조회수 가져오기
        MvcResult initialResult = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token1)  // 첫 번째 회원으로 조회
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String initialResponseContent = initialResult.getResponse().getContentAsString();
        ApiResponse<PostDetailDto> initialApiResponse = objectMapper.readValue(initialResponseContent, new TypeReference<ApiResponse<PostDetailDto>>() {
        });
        PostDetailDto initialPostDetail = initialApiResponse.getResponse();
        int initialViews = initialPostDetail.getViewes(); // 초기 조회수

        // When: 각 회원이 랜덤 조회수 만큼 게시글을 조회
        // 회원1이 랜덤 조회수 만큼 조회
        for (int i = 0; i < viewsForMember1; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token1)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        // 회원2가 랜덤 조회수 만큼 조회
        for (int i = 0; i < viewsForMember2; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token2)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        // 회원3이 랜덤 조회수 만큼 조회
        for (int i = 0; i < viewsForMember3; i++) {
            mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                            .header("Authorization", "Bearer " + token3)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
        }

        // 2. 조회수 증가 후 게시글 조회
        MvcResult finalResult = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, postId)
                        .header("Authorization", "Bearer " + token1)  // 마지막 회원으로 다시 조회
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        String finalResponseContent = finalResult.getResponse().getContentAsString();
        ApiResponse<PostDetailDto> finalApiResponse = objectMapper.readValue(finalResponseContent, new TypeReference<ApiResponse<PostDetailDto>>() {
        });
        PostDetailDto finalPostDetail = finalApiResponse.getResponse();
        int finalViews = finalPostDetail.getViewes(); // 최종 조회수

        // Then: 조회수 증가 여부 확인
        int expectedViews = initialViews + viewsForMember1 + viewsForMember2 + viewsForMember3 + 1;
        assertThat(finalViews).isEqualTo(expectedViews); // 조회수는 랜덤 조회수만큼 증가해야 함
    }


    @Test
    @DisplayName("존재하지 않는 카테고리 아이디로 접근")
    void readPostFailByNotExistCategoryTest() throws Exception {

        // Given: 회원 토큰 생성
        Long nonExistCategoryId = testCategoryId + 9999L;
        String token = jwtUtil.generateToken("testWriter", MemberGrade.GENERAL, postId);

        // When: 토큰 인증 후 조회 요청
        MvcResult result = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", nonExistCategoryId, postId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value()); // 404 상태 코드

        ApiResponse<PostDetailDto> apiResponse = objectMapper.readValue(responseContent, new TypeReference<ApiResponse<PostDetailDto>>() {
        });
        assertThat(apiResponse.isSuccess()).isFalse();


    }


    @Test
    @DisplayName("존재하지 않는 포스트 아이디로 접근")
    void readPostFailByNotExistPostTest() throws Exception {

        // Given: 회원 토큰 생성
        Long nonExistPostId = postId + 9999L;
        String token = jwtUtil.generateToken("testWriter", MemberGrade.GENERAL, postId);

        // When: 토큰 인증 후 조회 요청
        MvcResult result = mockMvc.perform(get("/posts/category/{categoryId}/detail/{postId}", testCategoryId, nonExistPostId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertThat(result.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value()); // 404 상태 코드

        ApiResponse<PostDetailDto> apiResponse = objectMapper.readValue(responseContent, new TypeReference<ApiResponse<PostDetailDto>>() {
        });
        assertThat(apiResponse.isSuccess()).isFalse();


    }


}
