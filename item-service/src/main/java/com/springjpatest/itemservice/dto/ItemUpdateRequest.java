package com.springjpatest.itemservice.dto;

import com.springjpatest.itemservice.entity.ItemConst;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 아이템 수정 요청 DTO (record).
 *
 * <p>수정 가능한 필드: 제목, 상품 종류, 가격, 판매 수량.
 * 서버와 판매자 이름은 수정 불가(등록 시 고정).
 * </p>
 */
public record ItemUpdateRequest(

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
    String title,

    @NotNull(message = "상품 종류는 필수입니다.")
    ItemConst.ItemType itemType,

    /** 가격 — 양수(0 초과)만 허용 */
    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    BigDecimal price,

    /** 수량 — 양수(0 초과)만 허용 */
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 양수여야 합니다.")
    Integer quantity

) {}
