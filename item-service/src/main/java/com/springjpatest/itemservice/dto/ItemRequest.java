package com.springjpatest.itemservice.dto;

import com.springjpatest.itemservice.entity.ItemConst;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 아이템 등록 요청 DTO (record).
 *
 * <p>record의 컴포넌트에 직접 Bean Validation 어노테이션을 선언한다.
 * Jackson은 Spring Boot 3.x(Jackson 2.14+)에서 record를 정식 지원하므로
 * @RequestBody 역직렬화에 별도 설정이 필요 없다.
 * </p>
 *
 * <p>검증 항목:
 * <ul>
 *   <li>server: 필수, 최대 50자</li>
 *   <li>sellerName: 필수, 최대 50자</li>
 *   <li>itemType: 필수 (정의된 enum 값만 허용)</li>
 *   <li>title: 필수, 최대 100자</li>
 *   <li>price: 필수, 양수(0 초과)</li>
 *   <li>quantity: 필수, 양수(0 초과)</li>
 * </ul>
 * </p>
 */
public record ItemRequest(

    @NotBlank(message = "서버는 필수입니다.")
    @Size(max = 50, message = "서버명은 50자 이하로 입력해주세요.")
    String server,

    @NotBlank(message = "판매자 이름은 필수입니다.")
    @Size(max = 50, message = "판매자 이름은 50자 이하로 입력해주세요.")
    String sellerName,

    @NotNull(message = "상품 종류는 필수입니다.")
    ItemConst.ItemType itemType,

    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하로 입력해주세요.")
    String title,

    /** 가격 — 양수(0 초과)만 허용 */
    @NotNull(message = "가격은 필수입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    BigDecimal price,

    /** 수량 — 양수(0 초과)만 허용 */
    @NotNull(message = "수량은 필수입니다.")
    @Positive(message = "수량은 양수여야 합니다.")
    Integer quantity

) {}
