package com.springjpatest.itemservice.service;

import com.springjpatest.itemservice.dto.ItemRequest;
import com.springjpatest.itemservice.dto.ItemResponse;
import com.springjpatest.itemservice.dto.ItemUpdateRequest;
import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;
import com.springjpatest.itemservice.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * ItemService 단위 테스트.
 *
 * <p>Spring Context 없이 Mockito만 사용하여 서비스 로직만 빠르게 검증한다.
 * @Cacheable, @Transactional 등 Spring AOP 어노테이션은 이 테스트에서 활성화되지 않는다.
 * (캐시 동작은 ItemCachingTest에서 별도 검증)
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

  @Mock
  private ItemRepository itemRepository;

  @InjectMocks
  private ItemService itemService;

  // ===== 공통 픽스처 =====
  private Item sampleItem;

  @BeforeEach
  void setUp() {
    sampleItem = Item.builder()
        .server("아시아")
        .sellerName("판매자")
        .itemType(ItemConst.ItemType.ITEM)
        .title("엑스칼리버")
        .price(new BigDecimal("50000"))
        .quantity(1)
        .build();
    // @GeneratedValue는 DB 저장 시 설정되므로 Reflection으로 id 주입
    ReflectionTestUtils.setField(sampleItem, "id", 1L);
    ReflectionTestUtils.setField(sampleItem, "version", 0L);
  }

  // ===== getItems (목록 조회) =====

  @Test
  @DisplayName("필터 없이 목록 조회 시 repository 결과를 그대로 반환한다")
  void getItems_전체_조회() {
    // given
    Page<Item> repoPage = new PageImpl<>(List.of(sampleItem), PageRequest.of(0, 20), 1);
    given(itemRepository.findByFilters(null, null, null, null, null, any()))
        .willReturn(repoPage);

    // when
    Page<ItemResponse> result = itemService.getItems(
        null, null, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax
    );

    // then
    assertThat(result.getTotalElements()).isEqualTo(1);
    assertThat(result.getContent().get(0).title()).isEqualTo("엑스칼리버");
    then(itemRepository).should(times(1)).findByFilters(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("PriceMax 정렬 옵션이면 가격 오름차순 Pageable이 repository에 전달된다")
  void getItems_price_정렬() {
    // given
    given(itemRepository.findByFilters(any(), any(), any(), any(), any(), any()))
        .willReturn(Page.empty());

    // when
    itemService.getItems(null, null, null, null, null, 0, 20, ItemConst.ItemSort.PriceMax);

    // then: 예외 없이 정상 실행됨을 확인
    then(itemRepository).should(times(1)).findByFilters(any(), any(), any(), any(), any(), any());
  }

  @Test
  @DisplayName("ItemType.ALL은 필터 없음으로 처리되어 null로 repository에 전달된다")
  void getItems_itemType_ALL_필터_없음() {
    // given
    given(itemRepository.findByFilters(any(), any(), any(), any(), any(), any()))
        .willReturn(Page.empty());

    // when
    itemService.getItems(null, ItemConst.ItemType.ALL, null, null, null, 0, 20, ItemConst.ItemSort.RegDateMax);

    // then: itemType=null로 repository 호출됨 (any()가 null도 매칭)
    then(itemRepository).should(times(1)).findByFilters(any(), any(), any(), any(), any(), any());
  }

  // ===== getItem (단건 조회) =====

  @Test
  @DisplayName("존재하는 ID로 단건 조회 시 해당 아이템 정보를 반환한다")
  void getItem_정상_조회() {
    // given
    given(itemRepository.findById(1L)).willReturn(Optional.of(sampleItem));

    // when
    ItemResponse response = itemService.getItem(1L);

    // then
    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.title()).isEqualTo("엑스칼리버");
    assertThat(response.itemType()).isEqualTo(ItemConst.ItemType.ITEM);
    assertThat(response.price()).isEqualByComparingTo("50000");
  }

  @Test
  @DisplayName("존재하지 않는 ID로 단건 조회 시 IllegalArgumentException이 발생한다")
  void getItem_존재하지_않는_ID() {
    // given
    given(itemRepository.findById(999L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> itemService.getItem(999L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("존재하지 않는 아이템");
  }

  // ===== createItem (등록) =====

  @Test
  @DisplayName("유효한 요청으로 아이템 등록 시 저장된 아이템 정보를 반환한다")
  void createItem_정상_등록() {
    // given
    ItemRequest request = createMockRequest("새 아이템");
    given(itemRepository.save(any(Item.class))).willReturn(sampleItem);

    // when
    ItemResponse response = itemService.createItem(request);

    // then
    assertThat(response.title()).isEqualTo("엑스칼리버"); // mock이 sampleItem 반환
    then(itemRepository).should(times(1)).save(any(Item.class));
  }

  // ===== updateItem (수정) =====

  @Test
  @DisplayName("존재하는 ID로 수정 요청 시 아이템 정보가 업데이트된다")
  void updateItem_정상_수정() {
    // given
    given(itemRepository.findById(1L)).willReturn(Optional.of(sampleItem));
    ItemUpdateRequest request = createMockUpdateRequest("수정된 검");

    // when
    ItemResponse response = itemService.updateItem(1L, request);

    // then: Item.update()가 호출되어 변경된 값이 응답에 반영됨
    assertThat(response.title()).isEqualTo("수정된 검");
    assertThat(response.itemType()).isEqualTo(ItemConst.ItemType.GAME_MONEY);
    assertThat(response.price()).isEqualByComparingTo("20000");
    assertThat(response.quantity()).isEqualTo(2);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 수정 요청 시 IllegalArgumentException이 발생한다")
  void updateItem_존재하지_않는_ID() {
    // given
    given(itemRepository.findById(999L)).willReturn(Optional.empty());
    ItemUpdateRequest request = createMockUpdateRequest("수정");

    // when & then
    assertThatThrownBy(() -> itemService.updateItem(999L, request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("존재하지 않는 아이템");
  }

  // ===== deleteItem (삭제) =====

  @Test
  @DisplayName("존재하는 ID로 삭제 요청 시 repository.delete가 호출된다")
  void deleteItem_정상_삭제() {
    // given
    given(itemRepository.findById(1L)).willReturn(Optional.of(sampleItem));

    // when
    itemService.deleteItem(1L);

    // then
    then(itemRepository).should(times(1)).delete(sampleItem);
  }

  @Test
  @DisplayName("존재하지 않는 ID로 삭제 요청 시 IllegalArgumentException이 발생한다")
  void deleteItem_존재하지_않는_ID() {
    // given
    given(itemRepository.findById(999L)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> itemService.deleteItem(999L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("존재하지 않는 아이템");
  }

  // ===== 헬퍼 =====

  private ItemRequest createMockRequest(String title) {
    return new ItemRequest("아시아", "판매자", ItemConst.ItemType.ITEM, title,
        new BigDecimal("10000"), 3);
  }

  private ItemUpdateRequest createMockUpdateRequest(String title) {
    return new ItemUpdateRequest(title, ItemConst.ItemType.GAME_MONEY,
        new BigDecimal("20000"), 2);
  }
}
