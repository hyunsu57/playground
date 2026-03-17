package com.springjpatest.blog.dto;

import com.springjpatest.blog.entity.Post;

import java.time.LocalDateTime;

/**
 * 게시글 응답 DTO.
 *
 * <p>엔티티를 직접 노출하지 않고 필요한 필드만 선택적으로 반환한다.
 * 불변 레코드를 사용하여 직렬화 안정성을 보장한다.
 * </p>
 *
 * @param id          게시글 ID
 * @param title       제목
 * @param content     본문
 * @param summary     요약
 * @param authorEmail 작성자 이메일
 * @param published   공개 여부
 * @param createdAt   생성 시각
 * @param updatedAt   수정 시각
 */
public record PostResponse(
    Long id,
    String title,
    String content,
    String summary,
    String authorEmail,
    boolean published,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Post 엔티티를 PostResponse DTO로 변환하는 팩토리 메서드.
     *
     * @param post 변환할 Post 엔티티
     * @return 변환된 PostResponse 인스턴스
     */
    public static PostResponse from(Post post) {
        return new PostResponse(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getSummary(),
            post.getAuthorEmail(),
            post.isPublished(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}
