package com.springjpatest.blog.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 게시글 엔티티.
 *
 * <p>블로그 기술 포스트 하나를 나타내는 JPA 엔티티.
 * Auditing을 사용하여 생성/수정 시각을 자동 기록한다.
 * </p>
 */
@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 직접 생성 방지)
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate 활성화
public class Post {

    /** 게시글 고유 식별자 (자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 게시글 제목 (최대 200자) */
    @Column(nullable = false, length = 200)
    private String title;

    /** 게시글 본문 (마크다운 형식, 길이 제한 없음) */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 게시글 요약 (목록 페이지 미리보기용, 최대 500자) */
    @Column(length = 500)
    private String summary;

    /** 작성자 이메일 (게이트웨이에서 X-User-Email 헤더로 전달) */
    @Column(nullable = false)
    private String authorEmail;

    /** 게시글 공개 여부 (false면 초안) */
    @Column(nullable = false)
    private boolean published = false;

    /** 게시글 생성 시각 (자동 설정, 수정 불가) */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 게시글 최종 수정 시각 (자동 갱신) */
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 게시글 생성 팩토리 메서드.
     * Builder 패턴을 통해 불변 생성을 유도한다.
     */
    @Builder
    public Post(String title, String content, String summary, String authorEmail, boolean published) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.authorEmail = authorEmail;
        this.published = published;
    }

    /**
     * 게시글 내용을 수정한다.
     * 엔티티 내부에서 상태 변경 로직을 관리하는 도메인 메서드.
     *
     * @param title   수정할 제목
     * @param content 수정할 본문
     * @param summary 수정할 요약
     */
    public void update(String title, String content, String summary) {
        this.title = title;
        this.content = content;
        this.summary = summary;
    }

    /**
     * 게시글 공개 상태를 변경한다.
     *
     * @param published true면 공개, false면 초안
     */
    public void changePublishStatus(boolean published) {
        this.published = published;
    }
}
