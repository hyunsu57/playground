package com.springjpatest.blog.controller;

import com.springjpatest.blog.dto.PostRequest;
import com.springjpatest.blog.dto.PostResponse;
import com.springjpatest.blog.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 게시글 REST 컨트롤러.
 *
 * <p>블로그 게시글 CRUD API 엔드포인트를 제공한다.
 * 인증이 필요한 요청(생성/수정/삭제)은 게이트웨이가 JWT를 검증한 후
 * X-User-Email 헤더를 통해 작성자 이메일을 전달한다.
 * </p>
 *
 * <ul>
 *   <li>GET  /api/posts         → 공개 게시글 목록 (페이지네이션)</li>
 *   <li>GET  /api/posts/{id}    → 게시글 단건 조회</li>
 *   <li>POST /api/posts         → 게시글 생성 (인증 필요)</li>
 *   <li>PUT  /api/posts/{id}    → 게시글 수정 (작성자만)</li>
 *   <li>DELETE /api/posts/{id}  → 게시글 삭제 (작성자만)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 공개된 게시글 목록을 페이지 단위로 반환한다.
     * 기본적으로 최신 게시글 순(createdAt DESC)으로 10개씩 반환한다.
     *
     * @param pageable 페이지 정보 (쿼리 파라미터 ?page=0&size=10&sort=createdAt,desc)
     * @return 게시글 페이지
     */
    @GetMapping
    public ResponseEntity<Page<PostResponse>> getPublishedPosts(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable
    ) {
        return ResponseEntity.ok(postService.getPublishedPosts(pageable));
    }

    /**
     * ID로 게시글 단건을 조회한다.
     *
     * @param id 게시글 ID
     * @return 게시글 상세 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    /**
     * 새 게시글을 생성한다.
     * 게이트웨이에서 JWT 검증 후 X-User-Email 헤더로 작성자 이메일을 전달받는다.
     *
     * @param request     게시글 생성 요청 데이터
     * @param authorEmail 게이트웨이에서 전달한 작성자 이메일 헤더
     * @return 생성된 게시글 (HTTP 201 Created)
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody PostRequest request,
        @RequestHeader("X-User-Email") String authorEmail
    ) {
        PostResponse created = postService.createPost(request, authorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 기존 게시글을 수정한다. 작성자 본인만 수정 가능하다.
     *
     * @param id          수정할 게시글 ID
     * @param request     수정 데이터
     * @param authorEmail 요청자 이메일 헤더
     * @return 수정된 게시글
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody PostRequest request,
        @RequestHeader("X-User-Email") String authorEmail
    ) {
        return ResponseEntity.ok(postService.updatePost(id, request, authorEmail));
    }

    /**
     * 게시글을 삭제한다. 작성자 본인만 삭제 가능하다.
     *
     * @param id          삭제할 게시글 ID
     * @param authorEmail 요청자 이메일 헤더
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
        @PathVariable Long id,
        @RequestHeader("X-User-Email") String authorEmail
    ) {
        postService.deletePost(id, authorEmail);
        return ResponseEntity.noContent().build();
    }
}
