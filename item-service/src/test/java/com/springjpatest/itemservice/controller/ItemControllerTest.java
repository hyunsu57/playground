package com.springjpatest.itemservice.controller;

import com.springjpatest.itemservice.dto.ItemListResponse;
import com.springjpatest.itemservice.dto.ItemResponse;
import com.springjpatest.itemservice.entity.ItemConst;
import com.springjpatest.itemservice.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ItemController 슬라이스 테스트.
 *
 * <p>@WebMvcTest: Spring MVC 계층만 로드. Service는 @MockBean으로 대체.
 * 검증 규칙(필수 필드 누락, 양수 조건, enum 값 제한)과
 * HTTP 상태 코드/응답 구조를 검증한다.
 * </p>
 *
 * <p>스펙 상 필수 검증 항목:
 * <ul>
 *   <li>필수 필드 누락 → 400 Bad Request + 상세 에러 메시지</li>
 *   <li>수량/가격은 양수(0 초과)만 허용 — 0 또는 음수 → 400</li>
 *   <li>상품 종류(itemType)는 정의된 enum 값만 허용 → 400</li>
 *   <li>등록 성공 → 201 Created + 생성된 상품 정보</li>
 * </ul>
 * </p>
 */
@WebMvcTest(ItemController.class)
class ItemControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private ItemService itemService;

  private ItemResponse sampleResponse;

  @BeforeEach
  void setUp() {
    sampleResponse = buildResponse(1L, "엑스칼리버", ItemConst.ItemType.ITEM, "아시아",
        new BigDecimal("50000"), 1);
  }

  // =========================================================
  // GET /api/items — 목록 조회
  // =========================================================

  @Nested
  @DisplayName("목록 조회 GET /api/items")
  class 목록_조회 {

    // ----- 정상 케이스 -----

    @Test
    @DisplayName("파라미터 없이 조회하면 기본값(page=0, size=20, sort=RegDateMax)으로 200 OK를 반환한다")
    void 기본_조회_성공() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(),
          eq(0), eq(20), eq(ItemConst.ItemSort.RegDateMax)))
          .willReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 20), 1));

      mockMvc.perform(get("/api/items"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].title").value("엑스칼리버"))
          .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("검색/필터/페이지/정렬 파라미터가 서비스로 정상 전달된다")
    void 파라미터_전달() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(), any(), any(), any()))
          .willReturn(new PageImpl<>(List.of(sampleResponse), PageRequest.of(0, 10), 1));

      mockMvc.perform(get("/api/items")
              .param("keyword", "검")
              .param("itemType", "ITEM")
              .param("server", "아시아")
              .param("minPrice", "10000")
              .param("maxPrice", "100000")
              .param("page", "0")
              .param("size", "10")
              .param("sort", "PriceMax"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("결과가 없으면 200 OK + 빈 content를 반환한다 (에러 아님)")
    void 빈_결과() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(), any(), any(), any()))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      mockMvc.perform(get("/api/items"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(0)))
          .andExpect(jsonPath("$.totalElements").value(0));
    }

    // ----- 페이지 파라미터 검증 -----

    @Test
    @DisplayName("[페이지 검증] page=-1이면 400 Bad Request")
    void 검증_page_음수() throws Exception {
      mockMvc.perform(get("/api/items").param("page", "-1"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'page')]").exists());
    }

    @Test
    @DisplayName("[페이지 검증] size=0이면 400 Bad Request")
    void 검증_size_0() throws Exception {
      mockMvc.perform(get("/api/items").param("size", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'size')]").exists());
    }

    @Test
    @DisplayName("[페이지 검증] size=101이면 400 Bad Request (최대 100)")
    void 검증_size_초과() throws Exception {
      mockMvc.perform(get("/api/items").param("size", "101"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'size')]").exists());
    }

    @Test
    @DisplayName("[페이지 검증] size=100은 최대 허용값이므로 200 OK")
    void 검증_size_최대_허용() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(), eq(0), eq(100), any()))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

      mockMvc.perform(get("/api/items").param("size", "100"))
          .andExpect(status().isOk());
    }

    // ----- 가격 범위 파라미터 검증 -----

    @Test
    @DisplayName("[가격 검증] minPrice=0이면 양수가 아니므로 400 Bad Request")
    void 검증_minPrice_0() throws Exception {
      mockMvc.perform(get("/api/items").param("minPrice", "0"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'minPrice')]").exists());
    }

    @Test
    @DisplayName("[가격 검증] maxPrice가 음수이면 400 Bad Request")
    void 검증_maxPrice_음수() throws Exception {
      mockMvc.perform(get("/api/items").param("maxPrice", "-100"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'maxPrice')]").exists());
    }

    @Test
    @DisplayName("[가격 범위 검증] minPrice > maxPrice이면 400 Bad Request")
    void 검증_가격_범위_역전() throws Exception {
      // minPrice=5000, maxPrice=1000 → 최저가 > 최고가 → 범위 오류
      mockMvc.perform(get("/api/items")
              .param("minPrice", "5000")
              .param("maxPrice", "1000"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'validPriceRange')]").exists());
    }

    @Test
    @DisplayName("[가격 범위 검증] minPrice == maxPrice이면 단일 가격 조회로 허용한다")
    void 검증_가격_범위_동일_허용() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(), any(), any(), any()))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      mockMvc.perform(get("/api/items")
              .param("minPrice", "1000")
              .param("maxPrice", "1000"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("[가격 범위 검증] minPrice만 제공해도 유효하다 (maxPrice 없으면 상한 없음)")
    void 검증_minPrice_단독_허용() throws Exception {
      given(itemService.getItems(any(), any(), any(), any(), any(), any(), any(), any()))
          .willReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

      mockMvc.perform(get("/api/items").param("minPrice", "1000"))
          .andExpect(status().isOk());
    }

    // ----- 에러 응답 구조 -----

    @Test
    @DisplayName("[에러 응답 구조] 파라미터 검증 실패 시 status/message/details 필드를 포함한다")
    void 검증_실패_응답_구조() throws Exception {
      mockMvc.perform(get("/api/items").param("page", "-1"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.details").isArray())
          .andExpect(jsonPath("$.details[0].field").exists())
          .andExpect(jsonPath("$.details[0].message").exists());
    }
  }

  // =========================================================
  // GET /api/items/{id} — 단건 조회
  // =========================================================

  @Nested
  @DisplayName("단건 조회 GET /api/items/{id}")
  class 단건_조회 {

    @Test
    @DisplayName("존재하는 ID 조회 시 200 OK + 아이템 정보를 반환한다")
    void 조회_성공() throws Exception {
      given(itemService.getItem(1L)).willReturn(sampleResponse);

      mockMvc.perform(get("/api/items/1"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.title").value("엑스칼리버"))
          .andExpect(jsonPath("$.itemType").value("ITEM"))
          .andExpect(jsonPath("$.price").value(50000));
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 404 Not Found + 에러 메시지를 반환한다")
    void 없는_ID_조회() throws Exception {
      given(itemService.getItem(999L))
          .willThrow(new IllegalArgumentException("존재하지 않는 아이템입니다. id=999"));

      mockMvc.perform(get("/api/items/999"))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.status").value(404))
          .andExpect(jsonPath("$.message").value(containsString("존재하지 않는 아이템")));
    }
  }

  // =========================================================
  // POST /api/items — 등록 (스펙 상 필수 검증 항목 집중 테스트)
  // =========================================================

  @Nested
  @DisplayName("아이템 등록 POST /api/items")
  class 아이템_등록 {

    @Test
    @DisplayName("모든 필드가 유효하면 201 Created + 생성된 아이템 정보를 반환한다")
    void 등록_성공_201() throws Exception {
      given(itemService.createItem(any())).willReturn(sampleResponse);

      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "GAME_MONEY",
                    "title": "다야 팝니다",
                    "price": 25470,
                    "quantity": 3000
                  }
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.title").value("엑스칼리버"));
    }

    // ----- 필수 필드 누락 체크 -----

    @Test
    @DisplayName("[필수 필드 누락] title이 없으면 400 + 필드 에러 반환")
    void 검증_title_누락() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "GAME_MONEY",
                    "price": 25470,
                    "quantity": 3000
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.details[?(@.field == 'title')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] title이 공백 문자열이면 400 + 필드 에러 반환")
    void 검증_title_공백() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "GAME_MONEY",
                    "title": "   ",
                    "price": 25470,
                    "quantity": 3000
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'title')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] server가 없으면 400 + 필드 에러 반환")
    void 검증_server_누락() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'server')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] sellerName이 없으면 400 + 필드 에러 반환")
    void 검증_sellerName_누락() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'sellerName')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] price가 없으면 400 + 필드 에러 반환")
    void 검증_price_누락() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'price')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] quantity가 없으면 400 + 필드 에러 반환")
    void 검증_quantity_누락() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'quantity')]").exists());
    }

    // ----- 수량/가격은 양수만 허용 (스펙 핵심 검증) -----

    @Test
    @DisplayName("[가격 양수 조건] price=0은 양수가 아니므로 400 Bad Request")
    void 검증_price_0_거절() throws Exception {
      // 스펙: "가격은 양수만 허용" → 0은 양수가 아님
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 0,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'price')]").exists());
    }

    @Test
    @DisplayName("[가격 양수 조건] price가 음수이면 400 Bad Request")
    void 검증_price_음수_거절() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": -1,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'price')]").exists());
    }

    @Test
    @DisplayName("[가격 양수 조건] price=1은 양수이므로 등록 성공")
    void 검증_price_최솟값_허용() throws Exception {
      given(itemService.createItem(any())).willReturn(sampleResponse);

      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("[수량 양수 조건] quantity=0은 양수가 아니므로 400 Bad Request")
    void 검증_quantity_0_거절() throws Exception {
      // 스펙: "수량은 양수만 허용" → 0은 양수가 아님
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 0
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'quantity')]").exists());
    }

    @Test
    @DisplayName("[수량 양수 조건] quantity가 음수이면 400 Bad Request")
    void 검증_quantity_음수_거절() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": -5
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'quantity')]").exists());
    }

    @Test
    @DisplayName("[수량 양수 조건] quantity=1은 양수이므로 등록 성공")
    void 검증_quantity_최솟값_허용() throws Exception {
      given(itemService.createItem(any())).willReturn(sampleResponse);

      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isCreated());
    }

    // ----- 상품 종류는 정의된 enum 값만 허용 -----

    @Test
    @DisplayName("[enum 제한] itemType이 null이면 400 Bad Request")
    void 검증_itemType_null_거절() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'itemType')]").exists());
    }

    @Test
    @DisplayName("[enum 제한] 정의되지 않은 itemType 값이면 400 Bad Request (JSON 파싱 오류)")
    void 검증_itemType_잘못된_값_거절() throws Exception {
      // GAME_MONEY, ITEM, ACCOUNT, ETC, ALL 외의 값은 거절
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "UNKNOWN_TYPE",
                    "title": "검 팝니다",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("[enum 제한] 정의된 enum 값(GAME_MONEY/ITEM/ACCOUNT/ETC)은 허용된다")
    void 검증_itemType_유효한_enum_허용() throws Exception {
      given(itemService.createItem(any())).willReturn(sampleResponse);

      // ALL은 필터용이므로 등록 가능한 실제 타입만 테스트
      List<ItemConst.ItemType> registrableTypes = List.of(
          ItemConst.ItemType.GAME_MONEY,
          ItemConst.ItemType.ITEM,
          ItemConst.ItemType.ACCOUNT,
          ItemConst.ItemType.ETC
      );

      for (ItemConst.ItemType type : registrableTypes) {
        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "server": "라엘08",
                      "sellerName": "아리",
                      "itemType": "%s",
                      "title": "검 팝니다",
                      "price": 1000,
                      "quantity": 1
                    }
                    """.formatted(type.name())))
            .andExpect(status().isCreated());
      }
    }

    // ----- 검증 실패 시 응답 구조 -----

    @Test
    @DisplayName("[에러 응답 구조] 검증 실패 시 status/error/message/details 필드를 포함한다")
    void 검증_실패_응답_구조() throws Exception {
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "server": "라엘08",
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.status").value(400))
          .andExpect(jsonPath("$.error").exists())
          .andExpect(jsonPath("$.message").exists())
          .andExpect(jsonPath("$.details").isArray())
          .andExpect(jsonPath("$.details[0].field").exists())
          .andExpect(jsonPath("$.details[0].message").exists());
    }

    @Test
    @DisplayName("[다중 검증 실패] 여러 필드가 동시에 실패하면 details에 각 필드 에러가 모두 포함된다")
    void 검증_다중_실패_모두_포함() throws Exception {
      // title 공백 + server 누락 + price=0 + quantity=0 → 4개 에러
      mockMvc.perform(post("/api/items")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "sellerName": "아리",
                    "itemType": "ITEM",
                    "title": "",
                    "price": 0,
                    "quantity": 0
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(4))))
          .andExpect(jsonPath("$.details[?(@.field == 'title')]").exists())
          .andExpect(jsonPath("$.details[?(@.field == 'server')]").exists())
          .andExpect(jsonPath("$.details[?(@.field == 'price')]").exists())
          .andExpect(jsonPath("$.details[?(@.field == 'quantity')]").exists());
    }
  }

  // =========================================================
  // PUT /api/items/{id} — 수정 (title, itemType, price, quantity만 수정 가능)
  // =========================================================

  @Nested
  @DisplayName("아이템 수정 PUT /api/items/{id}")
  class 아이템_수정 {

    @Test
    @DisplayName("유효한 요청이면 200 OK + 수정된 아이템 정보를 반환한다")
    void 수정_성공() throws Exception {
      ItemResponse updated = buildResponse(1L, "수정된 검", ItemConst.ItemType.ACCOUNT, "아시아",
          new BigDecimal("30000"), 2);
      given(itemService.updateItem(eq(1L), any())).willReturn(updated);

      mockMvc.perform(put("/api/items/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "title": "수정된 검",
                    "itemType": "ACCOUNT",
                    "price": 30000,
                    "quantity": 2
                  }
                  """))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.title").value("수정된 검"))
          .andExpect(jsonPath("$.itemType").value("ACCOUNT"));
    }

    @Test
    @DisplayName("[양수 조건] 수정 요청에서 price=0이면 400 Bad Request")
    void 수정_price_0_거절() throws Exception {
      mockMvc.perform(put("/api/items/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "title": "수정된 검",
                    "itemType": "ITEM",
                    "price": 0,
                    "quantity": 2
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'price')]").exists());
    }

    @Test
    @DisplayName("[양수 조건] 수정 요청에서 quantity=0이면 400 Bad Request")
    void 수정_quantity_0_거절() throws Exception {
      mockMvc.perform(put("/api/items/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "title": "수정된 검",
                    "itemType": "ITEM",
                    "price": 1000,
                    "quantity": 0
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'quantity')]").exists());
    }

    @Test
    @DisplayName("[필수 필드 누락] 수정 요청에서 title이 없으면 400 Bad Request")
    void 수정_title_누락_거절() throws Exception {
      mockMvc.perform(put("/api/items/1")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                  {
                    "itemType": "ITEM",
                    "price": 1000,
                    "quantity": 1
                  }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details[?(@.field == 'title')]").exists());
    }
  }

  // =========================================================
  // DELETE /api/items/{id} — 삭제
  // =========================================================

  @Nested
  @DisplayName("아이템 삭제 DELETE /api/items/{id}")
  class 아이템_삭제 {

    @Test
    @DisplayName("존재하는 ID 삭제 시 204 No Content를 반환한다")
    void 삭제_성공() throws Exception {
      willDoNothing().given(itemService).deleteItem(1L);

      mockMvc.perform(delete("/api/items/1"))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 ID 삭제 시 404 Not Found를 반환한다")
    void 삭제_없는_ID() throws Exception {
      willThrow(new IllegalArgumentException("존재하지 않는 아이템입니다. id=999"))
          .given(itemService).deleteItem(999L);

      mockMvc.perform(delete("/api/items/999"))
          .andExpect(status().isNotFound());
    }
  }

  // =========================================================
  // 헬퍼
  // =========================================================

  // ItemResponse가 record이므로 생성자를 직접 호출
  private ItemResponse buildResponse(Long id, String title, ItemConst.ItemType itemType,
      String server, BigDecimal price, int quantity) {
    return new ItemResponse(id, server, "판매자", itemType, title, price, quantity,
        LocalDateTime.now());
  }
}
