package flab.Linkedlog.service;

import flab.Linkedlog.dto.post.CreatePostDto;
import flab.Linkedlog.dto.post.PostDetailDto;
import flab.Linkedlog.dto.post.PostListDto;
import flab.Linkedlog.entity.Post;
import flab.Linkedlog.repository.CategoryRepository;
import flab.Linkedlog.repository.MemberRepository;
import flab.Linkedlog.repository.PostRepository;
import jakarta.persistence.EntityNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;


    // 글 등록
    public Long createPost(CreatePostDto postDto, Long categoryId, Long memberId) {


        Post post = Post.builder()
                .member(memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new))
                .category(categoryRepository.findById(categoryId).orElseThrow(EntityNotFoundException::new))
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .build();

        return postRepository.save(post).getId();
    }


    // 글 조회 1: 특정 카테고리의 글들을 날짜 내림차순으로 출력
    @Transactional(readOnly = true)
    public List<PostListDto> getPostsByCategory(Long categoryId) {
        return postRepository.findPostListInCategory(categoryId).stream()
                .map(post -> {

                    // PostListDto 생성
                    return new PostListDto(
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
    public List<PostListDto> searchPostsByCategoryAndKeyword(Long categoryId, String keyword) {
        return postRepository.findPostListInCategoryContainKeyword(categoryId, keyword).stream()
                .map(post -> new PostListDto(
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

    @Transactional(readOnly = true)
    public Optional<PostDetailDto> getPostDetailById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

        return Optional.of(new PostDetailDto(
                post.getId(),
                post.getCategory().getId(),
                post.getMember().getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getViews(),
                post.getPrice()
        ));
    }
}
