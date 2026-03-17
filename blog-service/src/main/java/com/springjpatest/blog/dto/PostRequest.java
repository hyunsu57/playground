package com.springjpatest.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 게시글 생성/수정 요청 DTO.
 *
 * <p>클라이언트에서 전달하는 게시글 데이터를 담는 불변 레코드.
 * @Valid 어노테이션과 함께 사용하여 컨트롤러 진입 전에 데이터를 검증한다.
 * </p>
 *
 * @param title     게시글 제목 (필수, 1~200자)
 * @param content   게시글 본문 (필수)
 * @param summary   게시글 요약 (선택, 최대 500자)
 * @param published 공개 여부 (기본값 false)
 */
public record PostRequest(

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
    String title,

    @NotBlank(message = "본문은 필수입니다.")
    String content,

    @Size(max = 500, message = "요약은 500자 이내여야 합니다.")
    String summary,

    boolean published
) {
}
