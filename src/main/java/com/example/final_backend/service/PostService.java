package com.example.final_backend.service;

import com.example.final_backend.repository.UserRepository;
import com.example.final_backend.dto.PostDto;
import com.example.final_backend.entity.PostEntity;
import com.example.final_backend.entity.UserEntity;
import com.example.final_backend.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final UserPenaltyService userPenaltyService;
    private final CheckBadwordService checkBadwordService;


    // 게시글 작성
    @Transactional
    public PostDto.WritePostResponse createPost(String userId, PostDto.PostRequest postRequestDto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        // 사용자 제한 여부 확인
        userPenaltyService.checkUserLimit(user);

        PostEntity post = PostEntity.create(user, postRequestDto.getTitle(), postRequestDto.getContent());
        post = postRepository.save(post);

        post.setTitle(checkBadwordService.getFilteredText(postRequestDto.getTitle(), user, post));
        post.setContent(checkBadwordService.getFilteredText(postRequestDto.getContent(), user, post));

        PostEntity createdPost = postRepository.save(post);

        return PostDto.WritePostResponse.of(createdPost, user);
    }

    // 게시글 수정
    @Transactional
    public PostDto.WritePostResponse updatePost(String userId, int postId, PostDto.PostRequest postRequestPostDto) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        UserEntity user = post.getUser();

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        // 사용자 제한 여부 확인
        userPenaltyService.checkUserLimit(user);

        post.setTitle(checkBadwordService.getFilteredText(postRequestPostDto.getTitle(), user, post));
        post.setContent(checkBadwordService.getFilteredText(postRequestPostDto.getContent(), user, post));
        post.setUpdatedAt(LocalDateTime.now());

        PostEntity updatedPost = postRepository.save(post);

        return PostDto.WritePostResponse.of(updatedPost, user);
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(String userId, int postId) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId));

        // 작성자 확인
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }

    // 게시글 상세 조회
    @Transactional
    public PostDto.CheckDetailsResponse getPostById(int postId, boolean increaseView) {
        PostEntity post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: " + postId)); // <- 이 부분 요청대로 수정됨

        if (increaseView) {
            post.setCount(post.getCount() + 1);
            postRepository.save(post);
        }

        return PostDto.CheckDetailsResponse.of(post);
    }


    // 게시글 목록 (페이징) 조회
    public Page<PostDto.CheckPostResponse> getPostsWithPaging(Pageable pageable) {
        Page<Object[]> result = postRepository.findAllWithCommentCount(pageable);
        return result.map(obj -> {
            PostEntity post = (PostEntity) obj[0];
            Long commentCount = (Long) obj[1];

            post.setCommentCount(commentCount.intValue());
            return PostDto.CheckPostResponse.of(post);
        });
    }

    // 게시글 검색
    public Page<PostDto.CheckPostResponse> searchPosts(String keyword, Pageable pageable) {
        Page<Object[]> results = postRepository.findAllWithCommentCountByKeyword(keyword, pageable);

        return results.map(row -> {
            PostEntity post = (PostEntity) row[0];
            Long commentCount = (Long) row[1];

            // commentCount를 PostEntity에 임시 주입
            post.setCommentCount(commentCount.intValue());

            return PostDto.CheckPostResponse.of(post);
        });
    }

    // 내 게시글 조회
    public Page<PostDto.CheckPostResponse> getMyPosts(String userId, Pageable pageable) {
        return postRepository.findByUserId_Id(userId, pageable)
                .map(PostDto.CheckPostResponse::of);
    }
}
