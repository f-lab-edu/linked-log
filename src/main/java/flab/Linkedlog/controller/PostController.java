package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.CreatePostDto;
import flab.Linkedlog.dto.post.PostDetailDto;
import flab.Linkedlog.dto.post.PostListDto;
import flab.Linkedlog.service.PostService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 글 등록
    @PostMapping("/category/{categoryId}/write")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> createPost(@Valid @RequestBody CreatePostDto createPostDto,
                                        @PathVariable Long categoryId) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.
                getContext().
                getAuthentication().
                getPrincipal();
        Long memberId = userDetails.getMemberId();

        Long postId = postService.createPost(createPostDto, categoryId, memberId);
        return ApiResponse.success(postId);
    }

    // 글 조회 1: 특정 카테고리의 글 조회
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<PostListDto>> getPostsByCategory(@PathVariable Long categoryId) {
        List<PostListDto> posts = postService.getPostsByCategory(categoryId);

        return ApiResponse.success(posts);
    }

    // 글 조회 2: 특정 카테고리에서 키워드로 검색
    @GetMapping("/category/{categoryId}/search")
    @PreAuthorize("permitAll()")
    public ApiResponse<List<PostListDto>> searchPostsByCategoryAndKeyword(
            @PathVariable Long categoryId,
            @RequestParam String keyword) {
        List<PostListDto> posts = postService.searchPostsByCategoryAndKeyword(categoryId, keyword);
        return ApiResponse.success(posts);
    }

    // 글 상세 : 글 1개 조회
    @GetMapping("/category/{categoryId}/detail/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PostDetailDto> getPostDetail(
            @PathVariable Long categoryId,
            @PathVariable Long postId) {
        PostDetailDto post = postService.getPostDetailById(categoryId, postId)
                .orElse(null);
        return ApiResponse.success(post);
    }


}
