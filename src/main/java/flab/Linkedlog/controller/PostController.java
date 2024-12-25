package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.dto.post.PostDetailResponse;
import flab.Linkedlog.dto.post.PostListResponse;
import flab.Linkedlog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 글 등록
    @PostMapping("/category/{categoryId}/write")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> createPost(@Valid @RequestBody CreatePostRequest createPostRequest,
                                        @PathVariable Long categoryId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.
                getContext().
                getAuthentication().
                getPrincipal();
        Long memberId = userDetails.getMemberId();

        Long postId = postService.createPost(createPostRequest, categoryId, memberId);
        return ApiResponse.success(postId);
    }

    // 글 조회 1: 특정 카테고리의 글 조회
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<PostListResponse>> getPostsByCategory(@PathVariable Long categoryId) {
        List<PostListResponse> posts = postService.getPostsByCategory(categoryId);

        return ApiResponse.success(posts);
    }

    // 글 조회 2: 특정 카테고리에서 키워드로 검색
    @GetMapping("/category/{categoryId}/search")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<PostListResponse>> searchPostsByCategoryAndKeyword(
            @PathVariable Long categoryId,
            @RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("keyword must not be empty");
        }
        List<PostListResponse> posts = postService.searchPostsByCategoryAndKeyword(categoryId, keyword);
        return ApiResponse.success(posts);
    }

    // 글 상세 : 글 1개 조회
    @GetMapping("/category/{categoryId}/detail/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long categoryId,
            @PathVariable Long postId) {
        PostDetailResponse post = postService.getPostDetailById(categoryId, postId);
        return ApiResponse.success(post);
    }


}
