package com.springjpatest.itemservice.repository;

import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ItemRepository 단위 테스트.
 *
 * <p>@DataJpaTest: JPA 관련 빈만 로드하여 빠르게 실행.
 * 각 테스트는 @Transactional이 기본 적용되어 테스트 후 롤백된다.
 * </p>
 *
 * <p>@BeforeEach에서 deleteAll() + 테스트 픽스처 삽입을 통해
 * data.sql 샘플 데이터와 무관하게 독립적인 테스트 환경을 만든다.
 * </p>
 */
@DataJpaTest
@Import(ItemRepositoryTest.JpaAuditingConfig.class)
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",             // data.sql 실행 안 함 (테스트 독립성)
    "spring.jpa.defer-datasource-initialization=true"
})
class ItemRepositoryTest {

  /**
   * @DataJpaTest는 전체 ApplicationContext를 로드하지 않으므로
   * ItemServiceApplication의 @EnableJpaAuditing이 활성화되지 않는다.
   * @TestConfiguration으로 별도 활성화한다.
   */
  @Configuration
  @EnableJpaAuditing
  static class JpaAuditingConfig {
  }

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private TestEntityManager em;

  // ===== 테스트 픽스처 =====
  private Item item1;    // ITEM 타입, 아시아, 50000
  private Item item2;    // ITEM 타입, 유럽, 30000
  private Item item3;    // GAME_MONEY 타입, 아시아, 75000
  private Item item4;    // ETC 타입, 아시아, 3000

  @BeforeEach
  void setUp() {
    // data.sql 포함 모든 기존 데이터 제거 후 테스트 픽스처만 삽입
    itemRepository.deleteAll();
    em.flush();

    item1 = save("아시아", "판매자1", ItemConst.ItemType.ITEM, "엑스칼리버", new BigDecimal("50000"), 1);
    item2 = save("유럽", "판매자2", ItemConst.ItemType.ITEM, "드래곤슬레이어", new BigDecimal("30000"), 2);
    item3 = save("아시아", "판매자3", ItemConst.ItemType.GAME_MONEY, "골드 다야 판매", new BigDecimal("75000"), 5);
    item4 = save("아시아", "판매자4", ItemConst.ItemType.ETC, "마법 주문서", new BigDecimal("3000"), 100);

    em.flush();
    em.clear(); // 1차 캐시 비우기 → SELECT 쿼리 실제 실행 확인
  }

  // ===== 전체 조회 =====

  @Test
  @DisplayName("필터 없이 전체 조회 시 저장된 모든 아이템을 반환한다")
  void 전체_조회() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(4);
  }

  // ===== 키워드 검색 =====

  @Test
  @DisplayName("키워드 검색 시 상품명에 키워드가 포함된 아이템만 반환한다")
  void 키워드_검색_일치() {
    Page<Item> result = itemRepository.findByFilters(
        "드래곤", null, null, null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("드래곤슬레이어");
  }

  @Test
  @DisplayName("키워드 검색 시 일치하는 상품명이 없으면 빈 페이지를 반환한다")
  void 키워드_검색_불일치() {
    Page<Item> result = itemRepository.findByFilters(
        "존재하지않는키워드", null, null, null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isZero();
    assertThat(result.getContent()).isEmpty();
  }

  // ===== itemType 필터 =====

  @Test
  @DisplayName("itemType 필터 적용 시 해당 종류의 아이템만 반환한다")
  void itemType_필터() {
    Page<Item> result = itemRepository.findByFilters(
        null, ItemConst.ItemType.ITEM, null, null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent())
        .extracting(Item::getItemType)
        .containsOnly(ItemConst.ItemType.ITEM);
  }

  // ===== server 필터 =====

  @Test
  @DisplayName("server 필터 적용 시 해당 서버의 아이템만 반환한다")
  void server_필터() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, "유럽", null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getServer()).isEqualTo("유럽");
  }

  // ===== 가격 범위 필터 =====

  @Test
  @DisplayName("minPrice 필터 적용 시 해당 가격 이상의 아이템만 반환한다")
  void minPrice_필터() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, new BigDecimal("30000"), null,
        PageRequest.of(0, 20)
    );

    // item1(50000), item2(30000), item3(75000) → 3개
    assertThat(result.getTotalElements()).isEqualTo(3);
    assertThat(result.getContent())
        .extracting(Item::getPrice)
        .allMatch(price -> price.compareTo(new BigDecimal("30000")) >= 0);
  }

  @Test
  @DisplayName("maxPrice 필터 적용 시 해당 가격 이하의 아이템만 반환한다")
  void maxPrice_필터() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, null, new BigDecimal("30000"),
        PageRequest.of(0, 20)
    );

    // item2(30000), item4(3000) → 2개
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent())
        .extracting(Item::getPrice)
        .allMatch(price -> price.compareTo(new BigDecimal("30000")) <= 0);
  }

  @Test
  @DisplayName("minPrice와 maxPrice 동시 적용 시 범위 안의 아이템만 반환한다")
  void 가격_범위_필터() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, new BigDecimal("10000"), new BigDecimal("60000"),
        PageRequest.of(0, 20)
    );

    // item1(50000), item2(30000) → 2개
    assertThat(result.getTotalElements()).isEqualTo(2);
    assertThat(result.getContent())
        .extracting(Item::getPrice)
        .allMatch(price ->
            price.compareTo(new BigDecimal("10000")) >= 0
                && price.compareTo(new BigDecimal("60000")) <= 0);
  }

  // ===== 복합 필터 =====

  @Test
  @DisplayName("키워드 + itemType 복합 필터 시 두 조건을 모두 만족하는 아이템만 반환한다")
  void 복합_필터_키워드_itemType() {
    Page<Item> result = itemRepository.findByFilters(
        "엑스", ItemConst.ItemType.ITEM, null, null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("엑스칼리버");
  }

  @Test
  @DisplayName("itemType + server 복합 필터 시 두 조건을 모두 만족하는 아이템만 반환한다")
  void 복합_필터_itemType_server() {
    Page<Item> result = itemRepository.findByFilters(
        null, ItemConst.ItemType.ITEM, "아시아", null, null,
        PageRequest.of(0, 20)
    );

    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("엑스칼리버");
  }

  // ===== 페이지네이션 =====

  @Test
  @DisplayName("페이지 크기가 2이면 첫 페이지는 2개, 두 번째 페이지는 나머지를 반환한다")
  void 페이지네이션() {
    Page<Item> firstPage = itemRepository.findByFilters(
        null, null, null, null, null,
        PageRequest.of(0, 2)
    );
    Page<Item> secondPage = itemRepository.findByFilters(
        null, null, null, null, null,
        PageRequest.of(1, 2)
    );

    assertThat(firstPage.getContent()).hasSize(2);
    assertThat(secondPage.getContent()).hasSize(2);
    assertThat(firstPage.getTotalPages()).isEqualTo(2);
    assertThat(firstPage.getTotalElements()).isEqualTo(4);
  }

  // ===== 정렬 =====

  @Test
  @DisplayName("가격 오름차순 정렬 시 가장 싼 아이템이 첫 번째로 반환된다")
  void 가격_오름차순_정렬() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, null, null,
        PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "price"))
    );

    List<Item> items = result.getContent();
    assertThat(items.get(0).getPrice()).isEqualByComparingTo("3000");   // item4
    assertThat(items.get(items.size() - 1).getPrice()).isEqualByComparingTo("75000"); // item3
  }

  @Test
  @DisplayName("가격 내림차순 정렬 시 가장 비싼 아이템이 첫 번째로 반환된다")
  void 가격_내림차순_정렬() {
    Page<Item> result = itemRepository.findByFilters(
        null, null, null, null, null,
        PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "price"))
    );

    assertThat(result.getContent().get(0).getPrice()).isEqualByComparingTo("75000"); // item3
  }

  // ===== 헬퍼 =====

  private Item save(String server, String sellerName, ItemConst.ItemType itemType,
      String title, BigDecimal price, int quantity) {
    return itemRepository.save(
        Item.builder()
            .server(server)
            .sellerName(sellerName)
            .itemType(itemType)
            .title(title)
            .price(price)
            .quantity(quantity)
            .build()
    );
  }
}
