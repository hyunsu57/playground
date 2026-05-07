package com.springjpatest.itemservice.dto;

import com.springjpatest.itemservice.entity.ItemConst;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 아이템 목록 조회 요청 DTO (record).
 *
 * <p>{@code @ModelAttribute}로 쿼리 파라미터를 바인딩하며 {@code @Valid}로 일괄 검증한다.
 * Spring MVC 6.1+(Spring Boot 3.2+)부터 record에 대한 생성자 기반 바인딩을 지원한다.
 * </p>
 *
 * <p>page, size, sort는 쿼리 파라미터가 생략되면 null이 되므로
 * {@link #pageOrDefault()}, {@link #sizeOrDefault()}, {@link #sortOrDefault()} 메서드로
 * 기본값을 적용한다.
 * </p>
 *
 * <p>검증 항목:
 * <ul>
 *   <li>keyword: 최대 50자</li>
 *   <li>server: 최대 50자</li>
 *   <li>minPrice: 제공 시 양수(0 초과)</li>
 *   <li>maxPrice: 제공 시 양수(0 초과)</li>
 *   <li>minPrice &le; maxPrice: 두 값 모두 제공 시 범위 유효성</li>
 *   <li>page: 제공 시 0 이상</li>
 *   <li>size: 제공 시 1 이상 100 이하</li>
 * </ul>
 * </p>
 */
public record ItemListRequest(

    @Size(max = 50, message = "검색어는 50자 이하로 입력해주세요.")
    String keyword,

    ItemConst.ItemType itemType,

    @Size(max = 50, message = "서버명은 50자 이하로 입력해주세요.")
    String server,

    @Positive(message = "최저 가격은 양수여야 합니다.")
    BigDecimal minPrice,

    @Positive(message = "최고 가격은 양수여야 합니다.")
    BigDecimal maxPrice,

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Max(value = 100, message = "페이지 크기는 최대 100까지 허용합니다.")
    Integer size,

    ItemConst.ItemSort sort

) {

  /**
   * 가격 범위 유효성 검사.
   * minPrice와 maxPrice가 모두 제공된 경우 minPrice &le; maxPrice 이어야 한다.
   */
  @AssertTrue(message = "최저 가격은 최고 가격보다 클 수 없습니다.")
  public boolean isValidPriceRange() {
    if (minPrice == null || maxPrice == null) {
      return true;
    }
    return minPrice.compareTo(maxPrice) <= 0;
  }

  /** page 기본값: 0 (쿼리 파라미터 생략 시 적용) */
  public int pageOrDefault() {
    return page != null ? page : 0;
  }

  /** size 기본값: 20 (쿼리 파라미터 생략 시 적용) */
  public int sizeOrDefault() {
    return size != null ? size : 20;
  }

  /** sort 기본값: RegDateMax (최신순, 쿼리 파라미터 생략 시 적용) */
  public ItemConst.ItemSort sortOrDefault() {
    return sort != null ? sort : ItemConst.ItemSort.RegDateMax;
  }
}
