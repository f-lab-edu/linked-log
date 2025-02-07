package flab.Linkedlog.service;

import flab.Linkedlog.dto.post.CreatePostRequest;
import flab.Linkedlog.dto.post.PostDetailResponse;
import flab.Linkedlog.dto.post.PostListResponse;
import flab.Linkedlog.entity.Member;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.entity.PostImage;
import flab.Linkedlog.entity.PostPaidHistory;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.PostPaidHistoryRepository;
import flab.Linkedlog.repository.post.PostRepository;
import flab.Linkedlog.repository.postImage.PostImageRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final PostImageRepository postImageRepository;
    private final PostRepository postRepository;
    private final ImageUploadService imageUploadService;
    private final PostPaidHistoryRepository postPaidHistoryRepository;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${post-image.default-folder-path}")
    private String folderName;

    // 글 등록
    public Long createPost(CreatePostRequest createPostRequest, Long categoryId, Long memberId, List<MultipartFile> images) throws IOException {

        Post post = Post.builder()
                .member(memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new))
                .category(categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new))
                .title(createPostRequest.getTitle())
                .content(createPostRequest.getContent())
                .price(createPostRequest.getPrice())
                .build();

        postRepository.save(post);

        if (images != null && !images.isEmpty()) {
            List<CompletableFuture<String>> futureList = new ArrayList<>();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    // async
                    CompletableFuture<String> uploadFuture = imageUploadService.uploadPostImageAsync(image);
                    futureList.add(uploadFuture);
                }
            }

            List<String> imageUrls = futureList.stream()
                    .map(CompletableFuture::join)
                    .toList();

            List<PostImage> postImages = imageUrls.stream()
                    .map(url -> PostImage.builder()
                            .post(post)
                            .imageUrl(url)
                            .build())
                    .collect(Collectors.toList());

            postImageRepository.saveAll(postImages);
        }

        return post.getId();
    }

    // 글 조회 1: 특정 카테고리의 글들을 날짜 내림차순으로 출력
    @Transactional(readOnly = true)
    public PageImpl<PostListResponse> getPostsByCategory(Long categoryId, Pageable pageable) {
        PageImpl<Post> postsPage = postRepository.findPostListInCategory(categoryId, pageable);

        List<PostListResponse> postListResponses = postsPage.getContent().stream()
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

        return new PageImpl<>(postListResponses, pageable, postsPage.getTotalElements());
    }


    // 글 조회 2: 특정 카테고리의 글 중 제목 또는 내용에 특정 문자열 포함된 글을 출력
    @Transactional(readOnly = true)
    public PageImpl<PostListResponse> searchPostsByCategoryAndKeyword(Long categoryId, String keyword, Pageable pageable) {
        PageImpl<Post> postsPage = postRepository.findPostListInCategoryContainKeyword(categoryId, keyword, pageable);

        if (postsPage.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<PostListResponse> postListResponses = postsPage.getContent().stream()
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

        return new PageImpl<>(postListResponses, pageable, postsPage.getTotalElements());
    }

    // 글 상세
    @Transactional
    public PostDetailResponse getPostDetailById(Long categoryId, Long postId, Long memberId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        if (!post.getCategory().getId().equals(categoryId)) {
            throw new IllegalArgumentException("The post does not exist in the category");
        }

        if (BigDecimal.ZERO.compareTo(post.getPrice()) != 0) { // post의 가격이 0이 아니라면 유료
            // 4️⃣ 이미 구매한 게시글인지 확인
            boolean alreadyPaid = postPaidHistoryRepository.existsByMemberIdAndPostId(memberId, postId);

            if (!alreadyPaid) {
                // 5️⃣ 구매 가능한지 확인
                if (!canPostBePaid(member.getCashPoint(), post.getPrice())) {
                    throw new IllegalStateException("포인트가 부족하여 구매할 수 없습니다.");
                }

                // 6️⃣ 결제 진행 (포인트 차감)
                member.decreaseCashPoint(post.getPrice());
                memberRepository.save(member);

                // 7️⃣ 구매 기록 저장
                PostPaidHistory postPaidHistory = new PostPaidHistory(post, member);
                postPaidHistoryRepository.save(postPaidHistory);
            }
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

    private boolean canPostBePaid(BigDecimal memberPoint, BigDecimal postPoint) {
        if (memberPoint == null || postPoint == null) {
            throw new IllegalArgumentException("memberPoint and postPoint can not be null");
        }
        return BigDecimal.ZERO.compareTo(postPoint) == 0 || postPoint.compareTo(memberPoint) <= 0;
    }


}
