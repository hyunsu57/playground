package com.springjpatest.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 사용자 엔티티.
 *
 * <p>서비스에 가입한 사용자 정보를 저장하는 JPA 엔티티.
 * 비밀번호는 BCrypt로 암호화하여 저장하며, 평문 비밀번호는 절대 저장하지 않는다.
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 직접 생성 방지)
@EntityListeners(AuditingEntityListener.class)
public class User {

    /** 사용자 고유 식별자 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사용자 이메일 (로그인 ID로 사용, 중복 불가) */
    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt로 암호화된 비밀번호 (평문 저장 금지) */
    @Column(nullable = false)
    private String password;

    /** 사용자 닉네임 (화면 표시용) */
    @Column(nullable = false)
    private String nickname;

    /** 사용자 역할 (ROLE_USER 또는 ROLE_ADMIN) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    /** 계정 생성 시각 */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 사용자 생성 팩토리 메서드.
     *
     * @param email    이메일
     * @param password BCrypt 암호화된 비밀번호
     * @param nickname 닉네임
     */
    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.ROLE_USER; // 신규 가입자는 기본적으로 일반 사용자 역할
    }

    /**
     * 사용자 역할 열거형.
     * Spring Security의 역할 네이밍 컨벤션(ROLE_ 접두사)을 따른다.
     */
    public enum Role {
        ROLE_USER,   // 일반 사용자: 게시글 읽기, 댓글 작성 가능
        ROLE_ADMIN   // 관리자: 모든 게시글 관리 가능
    }
}
