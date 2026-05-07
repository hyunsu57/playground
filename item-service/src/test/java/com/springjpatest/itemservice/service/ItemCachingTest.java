package com.springjpatest.itemservice.service;

import com.springjpatest.itemservice.dto.ItemRequest;
import com.springjpatest.itemservice.dto.ItemUpdateRequest;
import com.springjpatest.itemservice.entity.ItemConst;
import com.springjpatest.itemservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * Spring Cache(@Cacheable, @CacheEvict) 동작 테스트.
 *
 * <p>@SpringBootTest: Spring AOP가 활성화되어야 @Cacheable 어노테이션이 동작한다.
 * @SpyBean으로 실제 ItemRepository를 감시하여 DB 호출 횟수로 캐시 히트 여부를 검증한다.
 * </p>
 *
 * <p>테스트 간 캐시 독립성을 위해 @BeforeEach에서 캐시를 전부 비운다.
 * </p>
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=true"
})
class ItemCachingTest {

  @Autowired
  private ItemService itemService;

  /**
   * @SpyBean: 실제 빈을 사용하되 메서드 호출 횟수 등을 Mockito로 검증할 수 있다.
   * (ItemRepository의 실제 구현이 H2 DB에 동작함)
   */
  @SpyBean
  private ItemRepository itemRepository;

  @Autowired
  private CacheManager cacheManager;

  @BeforeEach
  void clearCache() {
    // 테스트마다 독립적인 캐시 상태를 보장하기 위해 전체 무효화
    cacheManager.getCacheNames()
        .forEach(name -> {
          var cache = cacheManager.getCache(name);
          if (cache != null) {
            cache.clear();
          }
        });

    // 이전 테스트 데이터 정리
    itemRepository.deleteAll();
  }

  // ===== 캐시 HIT 검증 =====

  @Test
  @DisplayName("동일한 파라미터로 두 번 조회 시 두 번째는 캐시에서 반환되어 DB 호출이 1번만 발생한다")
  void 목록_조회_캐시_히트() {
    // when: 동일한 파라미터로 2번 호출
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: repository는 1번만 호출되어야 함 (두 번째는 캐시에서 반환)
    then(itemRepository).should(times(1))
        .findByFilters(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("파라미터가 다른 두 조회는 각각 DB를 호출한다 (   캐시 키가 다름)")
  void 목록_조회_다른_파라미터는_캐시_미적용() {
    // when: 파라미터가 다른 2번 호출
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);
    itemService.getItems("검", null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: 각각 다른 캐시 키 → 두 번 모두 DB 호출
    then(itemRepository).should(times(2))
        .findByFilters(any(), any(), any(), any(), any(), any());
  }

  // ===== 캐시 EVICT 검증 (등록) =====

  @Test
  @DisplayName("아이템 등록 후 목록 조회 시 캐시가 무효화되어 DB를 다시 호출한다")
  void 등록_후_캐시_무효화() {
    // given: 첫 번째 조회로 캐시 채우기
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // when: 등록 (캐시 무효화 트리거)
    itemService.createItem(createRequest("새 아이템"));

    // 다시 같은 파라미터로 조회
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: 등록 전 1번 + 등록 후 1번 = 총 2번 DB 호출
    then(itemRepository).should(times(2))
        .findByFilters(any(), any(), any(), any(), any(), any());
  }

  // ===== 캐시 EVICT 검증 (수정) =====

  @Test
  @DisplayName("아이템 수정 후 목록 조회 시 캐시가 무효화되어 DB를 다시 호출한다")
  void 수정_후_캐시_무효화() {
    // given: 아이템 생성 + 첫 번째 목록 조회로 캐시 채우기
    var created = itemService.createItem(createRequest("수정 테스트 아이템"));
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // when: 수정 (캐시 무효화 트리거)
    itemService.updateItem(created.id(), createUpdateRequest("수정된 이름"));

    // 다시 같은 파라미터로 조회
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: 수정 전 1번 + 수정 후 1번 = 총 2번 DB 호출
    then(itemRepository).should(times(2))
        .findByFilters(any(), any(), any(), any(), any(), any());
  }

  // ===== 캐시 EVICT 검증 (삭제) =====

  @Test
  @DisplayName("아이템 삭제 후 목록 조회 시 캐시가 무효화되어 DB를 다시 호출한다")
  void 삭제_후_캐시_무효화() {
    // given: 아이템 생성 + 첫 번째 목록 조회로 캐시 채우기
    var created = itemService.createItem(createRequest("삭제 테스트 아이템"));
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // when: 삭제 (캐시 무효화 트리거)
    itemService.deleteItem(created.id());

    // 다시 같은 파라미터로 조회
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: 삭제 전 1번 + 삭제 후 1번 = 총 2번 DB 호출
    then(itemRepository).should(times(2))
        .findByFilters(any(), any(), any(), any(), any(), any());
  }

  // ===== 헬퍼 =====

  private ItemRequest createRequest(String title) {
    return new ItemRequest("라엘08", "판매자", ItemConst.ItemType.ITEM, title,
        new BigDecimal("10000"), 1);
  }

  private ItemUpdateRequest createUpdateRequest(String title) {
    return new ItemUpdateRequest(title, ItemConst.ItemType.GAME_MONEY,
        new BigDecimal("20000"), 2);
  }
}
