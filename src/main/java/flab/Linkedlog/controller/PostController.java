package flab.Linkedlog.controller;

import flab.Linkedlog.config.CustomUserDetails;
import flab.Linkedlog.controller.response.ApiResponse;
import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.dto.post.PostDetailResponse;
import flab.Linkedlog.dto.post.PostListResponse;
import flab.Linkedlog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    // 글 등록
    @PostMapping(value = "/category/{categoryId}/write", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Long> createPost(
            @Valid @RequestPart CreatePostRequest createPostRequest,
            @PathVariable Long categoryId,
            @RequestPart(required = false) List<MultipartFile> images,
            CustomUserDetails userDetails) throws IOException {
        Long memberId = userDetails.getMemberId();

        if (createPostRequest.getTitle() == null || createPostRequest.getTitle().isBlank()) {
            throw new IllegalArgumentException();
        }
        if (createPostRequest.getContent() == null || createPostRequest.getContent().isBlank()) {
            throw new IllegalArgumentException();
        }
        if (images != null && images.size() > 20) {
            throw new IllegalArgumentException();
        }

        Long postId = postService.createPost(createPostRequest, categoryId, memberId, images);
        return ApiResponse.success(postId);
    }

    // 글 조회 1: 특정 카테고리의 글 조회
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("permitAll()")
    public ApiResponse<PageImpl<PostListResponse>> getPostsByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {

        if (pageable.getPageSize() <= 0) {
            PageImpl<PostListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return ApiResponse.success(emptyPage);
        }

        PageImpl<PostListResponse> posts = postService.getPostsByCategory(categoryId, pageable);
        return ApiResponse.success(posts);
    }

    // 글 조회 2: 특정 카테고리에서 키워드로 검색
    @GetMapping("/category/{categoryId}/search")
    @PreAuthorize("permitAll()")
    public ApiResponse<PageImpl<PostListResponse>> searchPostsByCategoryAndKeyword(
            @PathVariable Long categoryId,
            @RequestParam String keyword,
            Pageable pageable) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("keyword must not be empty");
        }

        if (pageable.getPageSize() <= 0) {
            PageImpl<PostListResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            return ApiResponse.success(emptyPage);
        }

        PageImpl<PostListResponse> posts = postService.searchPostsByCategoryAndKeyword(categoryId, keyword, pageable);
        return ApiResponse.success(posts);
    }

    // 글 상세 : 글 1개 조회
    @GetMapping("/category/{categoryId}/detail/{postId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long categoryId,
            @PathVariable Long postId,
            CustomUserDetails userDetails) {
        Long memberId = userDetails.getMemberId();
        PostDetailResponse post = postService.getPostDetailById(categoryId, postId, memberId);
        return ApiResponse.success(post);
    }


}
