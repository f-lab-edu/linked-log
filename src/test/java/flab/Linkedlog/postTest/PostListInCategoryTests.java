package flab.Linkedlog.postTest;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.controller.response.RestPageImpl;
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
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class PostListInCategoryTests {


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

    private Long[] categoryIds = new Long[3];
    private Long[] category1PostIds = new Long[3];
    private Long[] category2PostIds = new Long[5];
    private Long[] category3PostIds = new Long[7];

    private Long testWriterId;

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

        String[] categoryNameList = {"요리", "동물", "컴퓨터"};
        String[] categoryPostTitleList0 = {"알밥", "덮밥", "국밥"};
        String[] categoryPostTitleList1 = {"강아지들", "고양이들", "토끼들", "병아리들", "햄스터들"};
        String[] categoryPostTitleList2 = {"C 1강", "C 2강", "자바 1강", "자바 2강", "자바 3강", "파이썬 1강", "파이썬 2강"};
        String contentExample = "Test";

        for (int i = 0; i < categoryNameList.length; i++) {
            Category category = Category.builder()
                    .name(categoryNameList[i])
                    .build();

            categoryRepository.save(category);

            categoryIds[i] = category.getId();

            String[] categoryPostList = null;
            Long[] currentCategoryPostIds = null;

            if (i == 0) {
                categoryPostList = categoryPostTitleList0;
                currentCategoryPostIds = category1PostIds;
            } else if (i == 1) {
                categoryPostList = categoryPostTitleList1;
                currentCategoryPostIds = category2PostIds;
            } else {
                categoryPostList = categoryPostTitleList2;
                currentCategoryPostIds = category3PostIds;
            }

            for (int j = 0; j < categoryPostList.length; j++) {
                Post post = Post.builder()
                        .member(testWriter)
                        .title(categoryPostList[j])
                        .content(contentExample)
                        .category(category)
                        .build();

                postRepository.save(post);

                currentCategoryPostIds[j] = post.getId();
            }
        }

    }

    @AfterEach
    void tearDown() {

        postRepository.deleteAll();
        categoryRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("회원이 특정 카테고리의 글 조회")
    void getPostsByCategoryTest() throws Exception {
        // Given
        Long categoryId = categoryIds[2];

        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}", categoryId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<RestPageImpl<PostListResponse>> response = objectMapper.readValue(
                responseContent, new TypeReference<>() {
                }
        );

        // Then
        Page<PostListResponse> postsPage = response.getResponse();

        assertThat(postsPage.getContent()).hasSize(5);
        assertThat(postsPage.getTotalElements()).isEqualTo(7);
        assertThat(postsPage.getTotalPages()).isEqualTo(2);

        assertThat(postsPage.getContent().get(0).getTitle()).isEqualTo("파이썬 2강");
        assertThat(postsPage.getContent().get(1).getTitle()).isEqualTo("파이썬 1강");
        assertThat(postsPage.getContent().get(2).getTitle()).isEqualTo("자바 3강");
        assertThat(postsPage.getContent().get(3).getTitle()).isEqualTo("자바 2강");
        assertThat(postsPage.getContent().get(4).getTitle()).isEqualTo("자바 1강");
    }


    @Test
    @DisplayName("회원이 특정 카테고리의 글 조회")
    void getPostsInSeveralCategoriesTest() throws Exception {
        // Given
        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);
        String[][] postTitleLists = new String[3][10];

        for (int i = 0; i < 3; i++) {
            // When
            String responseContent = mockMvc.perform(get("/posts/category/{categoryId}", categoryIds[i])
                            .header("Authorization", "Bearer " + token)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            ApiResponse<RestPageImpl<PostListResponse>> response = objectMapper.readValue(responseContent,
                    new TypeReference<>() {
                    });
            RestPageImpl<PostListResponse> pageResponse = response.getResponse();

            for (int j = 0; j < pageResponse.getContent().size(); j++) {
                postTitleLists[i][j] = pageResponse.getContent().get(j).getTitle();
            }
        }

        // Then
        assertThat(postTitleLists[0]).containsExactly("국밥", "덮밥", "알밥", null, null, null, null, null, null, null);
        assertThat(postTitleLists[1]).containsExactly("햄스터들", "병아리들", "토끼들", "고양이들", "강아지들", null, null, null, null, null);
        assertThat(postTitleLists[2]).containsExactly("파이썬 2강", "파이썬 1강", "자바 3강", "자바 2강", "자바 1강", "C 2강", "C 1강", null, null, null);

    }
    
    @Test
    @DisplayName("회원이 빈 카테고리의 글 조회")
    void getPostsByEmptyCategoryTest() throws Exception {
        // Given

        Category emptyCategory = Category.builder()
                .name("empty Category Name")
                .build();

        categoryRepository.save(emptyCategory);
        Long emptyCategoryId = emptyCategory.getId();

        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}", emptyCategoryId)
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ApiResponse<RestPageImpl<PostListResponse>> response = objectMapper.readValue(
                responseContent, new TypeReference<>() {
                }
        );
        Page<PostListResponse> postsPage = response.getResponse();

        // Then
        assertThat(postsPage.getContent()).hasSize(0);
    }


}
