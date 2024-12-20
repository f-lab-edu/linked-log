package flab.Linkedlog.postTest;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.PostListDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        testWriter = memberRepository.save(testWriter); // 저장 후 ID를 가져옴
        testWriterId = testWriter.getId(); // 자동 생성된 ID를 저장

        String[] categoryNameList = {"요리", "동물", "컴퓨터"};
        String[] categoryPostTitleList0 = {"알밥", "덮밥", "국밥"};
        String[] categoryPostTitleList1 = {"강아지들", "고양이들", "토끼들", "병아리들", "햄스터들"};
        String[] categoryPostTitleList2 = {"C 1강", "C 2강", "자바 1강", "자바 2강", "자바 3강", "파이썬 1강", "파이썬 2강"};
        String contentExample = "Test";


        // 카테고리 3개 생성
        for (int i = 0; i < categoryNameList.length; i++) {
            // 카테고리 객체 생성 (Builder 패턴 사용)
            Category category = Category.builder()
                    .name(categoryNameList[i])
                    .build();

            // 카테고리 저장
            categoryRepository.save(category);

            // 카테고리 ID를 인덱스에 맞게 저장
            categoryIds[i] = category.getId();

            // 각 카테고리에 맞는 포스트 목록 생성
            String[] categoryPostList = null;
            Long[] currentCategoryPostIds = null;

            // 카테고리에 맞는 포스트 리스트 선택
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

            // 각 카테고리 포스트 생성
            for (int j = 0; j < categoryPostList.length; j++) {
                Post post = Post.builder()
                        .member(testWriter)
                        .title(categoryPostList[j])
                        .content(contentExample)
                        .category(category) // 포스트가 속할 카테고리 설정
                        .build();

                // 포스트 저장
                postRepository.save(post);

                // 포스트 ID를 카테고리별 ID 배열에 저장
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
        Long categoryId = categoryIds[2];  // 테스트할 카테고리 ID

        String token = jwtUtil.generateToken("testReader", MemberGrade.GENERAL, 10000L);

        // When
        String responseContent = mockMvc.perform(get("/posts/category/{categoryId}", categoryId)
                        .header("Authorization", "Bearer " + token))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // 응답을 ApiResponse 객체로 변환
        ApiResponse<List<PostListDto>> response = objectMapper.readValue(responseContent,
                new TypeReference<>() {
                });

        // Then
        assertThat(response.isSuccess()).isTrue();  // 성공적인 응답인지 확인
        assertThat(response.getResponse()).hasSize(7);  // 응답에 포함된 글의 수가 3개인지 확인
        assertThat(response.getResponse().get(0).getTitle()).isEqualTo("파이썬 2강");
        assertThat(response.getResponse().get(1).getTitle()).isEqualTo("파이썬 1강");
        assertThat(response.getResponse().get(2).getTitle()).isEqualTo("자바 3강");
        assertThat(response.getResponse().get(3).getTitle()).isEqualTo("자바 2강");
        assertThat(response.getResponse().get(4).getTitle()).isEqualTo("자바 1강");
        assertThat(response.getResponse().get(5).getTitle()).isEqualTo("C 2강");
        assertThat(response.getResponse().get(6).getTitle()).isEqualTo("C 1강");

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
                            .header("Authorization", "Bearer " + token))
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            // 응답을 ApiResponse 객체로 변환
            ApiResponse<List<PostListDto>> response = objectMapper.readValue(responseContent,
                    new TypeReference<>() {
                    });

            for (int j = 0; j < response.getResponse().size(); j++) {
                postTitleLists[i][j] = response.getResponse().get(j).getTitle();
            }
        }

        // Then
        // Then
        // 각 카테고리별로 확인해야 할 제목이 정확한지 검증
        assertThat(postTitleLists[0]).containsExactly("국밥", "덮밥", "알밥", null, null, null, null, null, null, null);  // 첫 번째 카테고리에서 제목 확인
        assertThat(postTitleLists[1]).containsExactly("햄스터들", "병아리들", "토끼들", "고양이들", "강아지들", null, null, null, null, null);  // 두 번째 카테고리에서 제목 확인
        assertThat(postTitleLists[2]).containsExactly("파이썬 2강", "파이썬 1강", "자바 3강", "자바 2강", "자바 1강", "C 2강", "C 1강", null, null, null);  // 세 번째 카테고리에서 제목 확인

    }


}
