package com.springjpatest.itemservice.repository;

import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

/**
 * 아이템 리포지토리.
 *
 * <p>JPA + JPQL을 사용하여 다중 필터(상품명 키워드 검색, itemType, server, 가격 범위) +
 * 페이지네이션을 한 번의 쿼리로 처리한다.
 * null 파라미터는 해당 조건을 무시하도록 JPQL에서 처리한다.
 * </p>
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

  /**
   * 아이템 목록 조회 (검색 + 필터 + 페이지네이션).
   *
   * <p>파라미터가 null이면 해당 조건을 적용하지 않는다.
   * {@code Pageable}의 Sort 정보로 등록일순/가격순 정렬을 지원한다.
   * </p>
   *
   * @param keyword  상품명 키워드 검색 (null이면 전체)
   * @param itemType 상품 종류 필터 (null이면 전체)
   * @param server   서버 필터 (null이면 전체)
   * @param minPrice 최저 가격 (null이면 하한 없음)
   * @param maxPrice 최고 가격 (null이면 상한 없음)
   * @param pageable 페이지네이션 + 정렬 정보
   * @return 필터링된 아이템 Page 객체
   */
  @Query("""
      SELECT i FROM Item i
      WHERE (:keyword IS NULL OR i.title LIKE %:keyword%)
        AND (:itemType IS NULL OR i.itemType = :itemType)
        AND (:server IS NULL OR i.server = :server)
        AND (:minPrice IS NULL OR i.price >= :minPrice)
        AND (:maxPrice IS NULL OR i.price <= :maxPrice)
      """)
  Page<Item> findByFilters(
      @Param("keyword") String keyword,
      @Param("itemType") ItemConst.ItemType itemType,
      @Param("server") String server,
      @Param("minPrice") BigDecimal minPrice,
      @Param("maxPrice") BigDecimal maxPrice,
      Pageable pageable
  );
}
