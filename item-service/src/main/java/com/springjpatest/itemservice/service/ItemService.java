package com.springjpatest.itemservice.service;

import com.springjpatest.itemservice.dto.ItemRequest;
import com.springjpatest.itemservice.dto.ItemResponse;
import com.springjpatest.itemservice.dto.ItemUpdateRequest;
import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;
import com.springjpatest.itemservice.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 아이템 서비스.
 *
 * <p>목록 조회에는 {@code @Cacheable}을 적용하여 반복 조회 비용을 줄인다.
 * 등록/수정/삭제 시에는 {@code @CacheEvict}로 캐시를 무효화한다.
 * </p>
 *
 * <p>동시 수정 충돌은 {@code Item} 엔티티의 {@code @Version} 필드로
 * 낙관적 잠금 처리한다. 충돌 시 {@link jakarta.persistence.OptimisticLockException}이
 * 발생하며 {@code GlobalExceptionHandler}에서 409 Conflict로 응답한다.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본을 읽기 전용으로 설정하여 불필요한 flush 방지
public class ItemService {

  private static final String CACHE_NAME = "items";

  private final ItemRepository itemRepository;

  /**
   * 아이템 목록 조회 (검색 + 필터 + 페이지네이션 + 정렬).
   *
   * <p>동일한 파라미터 조합에 대해 Caffeine 캐시를 사용한다.
   * 캐시 키: keyword + itemType + server + minPrice + maxPrice + page + size + sort
   * </p>
   *
   * @param keyword  상품명 키워드 (null이면 전체)
   * @param itemType 상품 종류 필터 (null 또는 ALL이면 전체)
   * @param server   서버 필터 (null이면 전체)
   * @param minPrice 최저 가격 (null이면 하한 없음)
   * @param maxPrice 최고 가격 (null이면 상한 없음)
   * @param page     페이지 번호 (0부터 시작)
   * @param size     페이지 크기
   * @param sort     정렬 기준 ({@link ItemConst.ItemSort})
   * @return 아이템 Page 객체
   */
  @Cacheable(
      value = CACHE_NAME,
      key = "#keyword + '_' + #itemType + '_' + #server + '_' + #minPrice + '_' + #maxPrice"
          + " + '_' + #page + '_' + #size + '_' + #sort"
  )
  public Page<ItemResponse> getItems(
      String keyword, ItemConst.ItemType itemType, String server,
      BigDecimal minPrice, BigDecimal maxPrice,
      int page, int size, ItemConst.ItemSort sort) {

    // ItemType.ALL은 필터 없음(null)으로 처리
    ItemConst.ItemType filterType = (itemType == null || itemType == ItemConst.ItemType.ALL)
        ? null : itemType;

    // 정렬 기준 파싱
    Sort sortOrder = switch (sort) {
      case RegDateMin -> Sort.by(Sort.Direction.ASC, "createdAt");   // 오래된순
      case RegDateMax -> Sort.by(Sort.Direction.DESC, "createdAt");  // 최신순
      case PriceMin -> Sort.by(Sort.Direction.DESC, "price");        // 높은 가격순
      case PriceMax -> Sort.by(Sort.Direction.ASC, "price");         // 낮은 가격순
      case ALL -> Sort.by(Sort.Direction.DESC, "createdAt");         // 기본: 최신순
    };

    Pageable pageable = PageRequest.of(page, size, sortOrder);
    return itemRepository.findByFilters(keyword, filterType, server, minPrice, maxPrice, pageable)
        .map(ItemResponse::from);
  }

  /**
   * 아이템 단건 조회.
   *
   * @param id 조회할 아이템 ID
   * @return 아이템 응답 DTO
   * @throws IllegalArgumentException 존재하지 않는 ID
   */
  public ItemResponse getItem(Long id) {
    Item item = findItemById(id);
    return ItemResponse.from(item);
  }

  /**
   * 아이템 등록.
   * 등록 후 목록 캐시를 전체 무효화하여 다음 조회 시 최신 데이터를 반환한다.
   *
   * @param request 등록 요청 DTO
   * @return 생성된 아이템 응답 DTO
   */
  @Transactional
  @CacheEvict(value = CACHE_NAME, allEntries = true) // 목록 캐시 전체 무효화
  public ItemResponse createItem(ItemRequest request) {
    Item item = Item.builder()
        .server(request.server())
        .sellerName(request.sellerName())
        .itemType(request.itemType())
        .title(request.title())
        .price(request.price())
        .quantity(request.quantity())
        .build();
    return ItemResponse.from(itemRepository.save(item));
  }

  /**
   * 아이템 수정 (제목, 상품 종류, 가격, 수량).
   *
   * <p>낙관적 잠금(@Version) 적용:
   * 동시에 두 트랜잭션이 같은 아이템을 수정하려 하면
   * 나중에 커밋되는 쪽에서 {@link jakarta.persistence.OptimisticLockException}이 발생한다.
   * </p>
   *
   * @param id      수정할 아이템 ID
   * @param request 수정 요청 DTO
   * @return 수정된 아이템 응답 DTO
   */
  @Transactional
  @CacheEvict(value = CACHE_NAME, allEntries = true) // 목록 캐시 전체 무효화
  public ItemResponse updateItem(Long id, ItemUpdateRequest request) {
    Item item = findItemById(id);
    item.update(
        request.title(),
        request.itemType(),
        request.price(),
        request.quantity()
    );
    // @Transactional이므로 명시적 save 없이 변경 감지(dirty checking)로 UPDATE 실행
    return ItemResponse.from(item);
  }

  /**
   * 아이템 삭제.
   *
   * @param id 삭제할 아이템 ID
   */
  @Transactional
  @CacheEvict(value = CACHE_NAME, allEntries = true) // 목록 캐시 전체 무효화
  public void deleteItem(Long id) {
    Item item = findItemById(id);
    itemRepository.delete(item);
  }

  /**
   * ID로 아이템을 조회하는 내부 헬퍼.
   * 존재하지 않으면 예외를 던져 컨트롤러에서 404로 처리한다.
   */
  private Item findItemById(Long id) {
    return itemRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이템입니다. id=" + id));
  }
}
