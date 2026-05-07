package com.springjpatest.itemservice.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 아이템 목록 조회 응답 DTO (record).
 *
 * <p>{@code Page<ItemResponse>}를 플랫 JSON 구조로 변환하여
 * 클라이언트가 pagination 정보를 쉽게 파악할 수 있도록 한다.
 * </p>
 *
 * <p>응답 구조:
 * <pre>
 * {
 *   "content": [...],
 *   "page": 0,
 *   "size": 20,
 *   "totalElements": 150,
 *   "totalPages": 8
 * }
 * </pre>
 * </p>
 */
public record ItemListResponse(
    List<ItemResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {

  /**
   * Spring Data Page 객체를 응답 DTO로 변환한다.
   *
   * @param page Page 객체
   * @return 목록 응답 DTO
   */
  public static ItemListResponse from(Page<ItemResponse> page) {
    return new ItemListResponse(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages()
    );
  }
}
