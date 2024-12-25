package flab.Linkedlog.postTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.PostListResponse;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PostSearchTests {


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

    private Long testWriterId;
    private Long testCategoryId;

    @BeforeEach
    void setUp() {
        Member testWriter = Member.builder()
                .userId("testWriter")
                .password("testPassword")
                .nickName("writerNickName")
                .email("writeremail@test.com")
                .phone("010-0000-0000")
                .memberGrade(MemberGrade.GENERAL)
                .build();
        testWriter = memberRepository.save(testWriter);
        testWriterId = testWriter.getId();

        String categoryName = "카테고리";
        String[] categoryPostTitleList = {"C 1강", "C 2강", "자바 1강", "자바 3강", "자바 5강", "파이썬 2강", "파이썬 7강"};
        String[] categoryPostContentList = {"C언어 개요", "C언어 메모리", "자바 개요", "자바 클래스", "자바 라이브러리", "파이썬 개요", "파이썬 라이브러리"};

        Category category = Category.builder()
                .name(categoryName)
                .build();

        categoryRepository.save(category);
        testCategoryId = category.getId();


        for (int i = 0; i < categoryPostTitleList.length; i++) {
            Post post = Post.builder()
                    .member(testWriter)
                    .title(categoryPostTitleList[i])
                    .content(categoryPostContentList[i])
                    .category(category)
                    .build();

            postRepository.save(post);

        }
    }

    @AfterEach
    void tearDown() {

        postRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }


    @Test
    @DisplayName("제목에 있으나 내용에 없는 키워드 입력")
    void getPostsContainKeywordOnlyInTitleTest() throws Exception {
        // Given
        Long categoryId = testCategoryId;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);
        String keyword = "2강";

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}/search", categoryId)
                        .param("keyword", keyword)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<List<PostListResponse>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });

        // Then
        assertThat(response.getResponse()).hasSize(2);
        assertThat(response.getResponse().get(0).getContent()).isEqualTo("파이썬 개요");
        assertThat(response.getResponse().get(1).getContent()).isEqualTo("C언어 메모리");

    }

    @Test
    @DisplayName("내용에 있으나 제목에 없는 키워드 입력")
    void getPostsContainKeywordOnlyInContentTest() throws Exception {
        // Given
        Long categoryId = testCategoryId;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);
        String keyword = " 라이브";

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}/search", categoryId)
                        .param("keyword", keyword)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<List<PostListResponse>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });

        // Then
        assertThat(response.getResponse()).hasSize(2);
        assertThat(response.getResponse().get(0).getTitle()).isEqualTo("파이썬 7강");
        assertThat(response.getResponse().get(1).getTitle()).isEqualTo("자바 5강");
    }

    @Test
    @DisplayName("제목, 내용 둘 다 존재하지 않는 키워드 입력")
    void getZeroPostsContainKeywordTest() throws Exception {
        // Given
        Long categoryId = testCategoryId;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);
        String keyword = "자바스크립트";

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}/search", categoryId)
                        .param("keyword", keyword)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<List<PostListResponse>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });
        // Then
        assertThat(response.getResponse()).hasSize(0);
    }


    @Test
    @DisplayName("키워드로 '' 입력")
    void getPostEmptyKeywordTest() throws Exception {
        // Given
        Long categoryId = testCategoryId;
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);
        String keyword = "";

        // When & Then
        mockMvc.perform(get("/posts/category/{categoryId}/search", categoryId)
                        .param("keyword", keyword)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }
}
