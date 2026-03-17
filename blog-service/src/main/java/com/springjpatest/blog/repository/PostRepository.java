package com.springjpatest.blog.repository;

import com.springjpatest.blog.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 게시글 레포지토리.
 *
 * <p>Spring Data JPA가 런타임에 구현체를 자동 생성한다.
 * 기본 CRUD 외에 블로그 서비스에 필요한 조회 메서드를 추가로 정의한다.
 * </p>
 */
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 공개된 게시글만 페이지 단위로 조회한다.
     * 목록 페이지에서 비공개(초안) 게시글을 숨기기 위해 사용한다.
     *
     * @param published 공개 여부 (true: 공개글만)
     * @param pageable  페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 조건에 맞는 게시글 페이지
     */
    Page<Post> findByPublished(boolean published, Pageable pageable);

    /**
     * 특정 작성자의 게시글을 페이지 단위로 조회한다.
     *
     * @param authorEmail 작성자 이메일
     * @param pageable    페이지 정보
     * @return 해당 작성자의 게시글 페이지
     */
    Page<Post> findByAuthorEmail(String authorEmail, Pageable pageable);
}
