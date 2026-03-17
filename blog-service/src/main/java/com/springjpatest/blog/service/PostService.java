package com.springjpatest.blog.service;

import com.springjpatest.blog.dto.PostRequest;
import com.springjpatest.blog.dto.PostResponse;
import com.springjpatest.blog.entity.Post;
import com.springjpatest.blog.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 게시글 비즈니스 로직 서비스.
 *
 * <p>게시글 CRUD에 관한 핵심 비즈니스 로직을 처리한다.
 * 모든 DB 접근은 트랜잭션 안에서 수행되며, 읽기 전용 메서드는
 * @Transactional(readOnly = true)로 성능을 최적화한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 (성능 최적화)
public class PostService {

    private final PostRepository postRepository;

    /**
     * 공개된 게시글 목록을 페이지 단위로 조회한다.
     *
     * @param pageable 페이지 정보 (번호, 크기, 정렬 기준)
     * @return 공개 게시글 페이지
     */
    public Page<PostResponse> getPublishedPosts(Pageable pageable) {
        return postRepository.findByPublished(true, pageable)
            .map(PostResponse::from);
    }

    /**
     * 게시글 ID로 단건 조회한다.
     *
     * @param id 게시글 ID
     * @return 게시글 응답 DTO
     * @throws IllegalArgumentException 게시글이 존재하지 않을 경우
     */
    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id));
        return PostResponse.from(post);
    }

    /**
     * 새 게시글을 생성한다.
     *
     * @param request     게시글 생성 요청 DTO
     * @param authorEmail 작성자 이메일 (게이트웨이에서 전달)
     * @return 생성된 게시글 응답 DTO
     */
    @Transactional // 쓰기 작업이므로 readOnly = false
    public PostResponse createPost(PostRequest request, String authorEmail) {
        Post post = Post.builder()
            .title(request.title())
            .content(request.content())
            .summary(request.summary())
            .authorEmail(authorEmail)
            .published(request.published())
            .build();

        Post savedPost = postRepository.save(post);
        return PostResponse.from(savedPost);
    }

    /**
     * 기존 게시글을 수정한다.
     * 작성자 본인만 수정 가능하도록 이메일을 비교한다.
     *
     * @param id          수정할 게시글 ID
     * @param request     수정 요청 DTO
     * @param authorEmail 요청자 이메일 (게이트웨이에서 전달)
     * @return 수정된 게시글 응답 DTO
     * @throws IllegalArgumentException 게시글 미존재 또는 권한 없음
     */
    @Transactional
    public PostResponse updatePost(Long id, PostRequest request, String authorEmail) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id));

        // 작성자 본인 확인
        if (!post.getAuthorEmail().equals(authorEmail)) {
            throw new IllegalArgumentException("게시글 수정 권한이 없습니다.");
        }

        post.update(request.title(), request.content(), request.summary());
        post.changePublishStatus(request.published());

        return PostResponse.from(post);
    }

    /**
     * 게시글을 삭제한다.
     * 작성자 본인만 삭제 가능하도록 이메일을 비교한다.
     *
     * @param id          삭제할 게시글 ID
     * @param authorEmail 요청자 이메일 (게이트웨이에서 전달)
     * @throws IllegalArgumentException 게시글 미존재 또는 권한 없음
     */
    @Transactional
    public void deletePost(Long id, String authorEmail) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id: " + id));

        if (!post.getAuthorEmail().equals(authorEmail)) {
            throw new IllegalArgumentException("게시글 삭제 권한이 없습니다.");
        }

        postRepository.delete(post);
    }
}
