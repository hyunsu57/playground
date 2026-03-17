package com.springjpatest.auth.repository;

import com.springjpatest.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 레포지토리.
 *
 * <p>사용자 데이터 접근을 담당한다.
 * Spring Data JPA가 런타임에 구현체를 자동 생성한다.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 이메일로 사용자를 조회한다.
     * 로그인 시 사용자 존재 여부 확인과 비밀번호 검증에 사용한다.
     *
     * @param email 조회할 이메일
     * @return 사용자 Optional (존재하지 않을 수 있음)
     */
    Optional<User> findByEmail(String email);

    /**
     * 이메일이 이미 사용 중인지 확인한다.
     * 회원가입 시 이메일 중복 검사에 사용한다.
     *
     * @param email 확인할 이메일
     * @return 이미 존재하면 true
     */
    boolean existsByEmail(String email);
}
