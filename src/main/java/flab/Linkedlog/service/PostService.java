package flab.Linkedlog.service;

import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.dto.post.PostDetailResponse;
import flab.Linkedlog.dto.post.PostListResponse;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.PostImage;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.postImage.PostImageRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final flab.Linkedlog.repository.post.PostRepository postRepository;
    private final S3Service s3Service;


    // 글 등록
    public Long createPost(CreatePostRequest postDto, Long categoryId, Long memberId, List<MultipartFile> images) throws IOException {

        if (postDto.getTitle() == null || postDto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title must not be empty");
        }
        if (postDto.getContent() == null || postDto.getContent().isBlank()) {
            throw new IllegalArgumentException("Content must not be empty");
        }
        if (images != null && images.size() > 20) {
            throw new IllegalArgumentException("You can upload up to 20 images only");
        }

        Post post = Post.builder()
                .member(memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new))
                .category(categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new))
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .build();

        postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String imageUrl = s3Service.uploadFile(image);
                    PostImage postImage = PostImage.builder()
                            .post(post)
                            .imageUrl(imageUrl)
                            .build();
                    postImages.add(postImage);
                }
            }
            postImageRepository.saveAll(postImages);
        }

        return post.getId();
    }


    // 글 조회 1: 특정 카테고리의 글들을 날짜 내림차순으로 출력
    @Transactional(readOnly = true)
    public List<PostListResponse> getPostsByCategory(Long categoryId) {
        return postRepository.findPostListInCategory(categoryId).stream()
                .map(post -> {

                    // PostListDto 생성
                    return new PostListResponse(
                            post.getId(),
                            post.getTitle(),
                            post.getContent(),
                            post.getCreatedAt(),
                            post.getCategory().getName(),
                            post.getMember().getNickName(),
                            post.getViews(),
                            post.getPrice()
                    );
                })
                .collect(Collectors.toList());
    }


    // 글 조회 2: 특정 카테고리의 글 중 제목 또는 내용에 특정 문자열 포함된 글을 출력
    @Transactional(readOnly = true)
    public List<PostListResponse> searchPostsByCategoryAndKeyword(Long categoryId, String keyword) {

        return postRepository.findPostListInCategoryContainKeyword(categoryId, keyword).stream()
                .map(post -> new PostListResponse(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getCreatedAt(),
                        post.getCategory().getName(),
                        post.getMember().getNickName(),
                        post.getViews(),
                        post.getPrice()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public PostDetailResponse getPostDetailById(Long categoryId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        if (!post.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("The post does not exist in the category");
        }

        post.incrementViews();

        List<String> imageUrls = postImageRepository.findAllByPostId(postId).stream()
                .map(PostImage::getImageUrl)
                .toList();

        return new PostDetailResponse(
                post.getId(),
                post.getCategory().getId(),
                post.getMember().getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getViews(),
                post.getPrice(),
                imageUrls
        );
    }
}
