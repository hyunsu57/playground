package com.springjpatest.itemservice.controller;

import com.springjpatest.itemservice.dto.ItemListRequest;
import com.springjpatest.itemservice.dto.ItemListResponse;
import com.springjpatest.itemservice.dto.ItemRequest;
import com.springjpatest.itemservice.dto.ItemResponse;
import com.springjpatest.itemservice.dto.ItemUpdateRequest;
import com.springjpatest.itemservice.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 아이템 REST 컨트롤러.
 *
 * <p>모든 엔드포인트는 {@code /api/items} 하위에 위치한다.
 * 목록 조회 파라미터는 {@link ItemListRequest}에 정의된 검증 규칙을 따른다.
 * </p>
 */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;

  /**
   * 아이템 목록 조회.
   * GET /api/items?keyword=검색어&itemType=GAME_MONEY&server=라엘08&minPrice=1000&maxPrice=500000&page=0&size=20&sort=RegDateMax
   *
   * <p>쿼리 파라미터는 {@link ItemListRequest}로 바인딩되며 {@code @Valid}로 일괄 검증된다.
   * 검증 실패 시 {@link org.springframework.validation.BindException}이 발생하여
   * {@code GlobalExceptionHandler}가 400 Bad Request로 응답한다.
   * </p>
   *
   * @param request 목록 조회 조건 (검색, 필터, 페이지, 정렬)
   * @return 200 OK + 페이지네이션 목록 ({@link ItemListResponse})
   */
  @GetMapping
  public ResponseEntity<ItemListResponse> getItems(
      @Valid @ModelAttribute ItemListRequest request) {

    Page<ItemResponse> result = itemService.getItems(
        request.keyword(), request.itemType(), request.server(),
        request.minPrice(), request.maxPrice(),
        request.pageOrDefault(), request.sizeOrDefault(), request.sortOrDefault()
    );
    return ResponseEntity.ok(ItemListResponse.from(result));
  }

  /**
   * 아이템 단건 조회.
   * GET /api/items/{id}
   *
   * @return 200 OK + 아이템 정보
   */
  @GetMapping("/{id}")
  public ResponseEntity<ItemResponse> getItem(@PathVariable Long id) {
    return ResponseEntity.ok(itemService.getItem(id));
  }

  /**
   * 아이템 등록.
   * POST /api/items
   *
   * @return 201 Created + 생성된 아이템 정보
   */
  @PostMapping
  public ResponseEntity<ItemResponse> createItem(@Valid @RequestBody ItemRequest request) {
    ItemResponse response = itemService.createItem(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * 아이템 수정 (제목, 상품 종류, 가격, 수량).
   * PUT /api/items/{id}
   *
   * @return 200 OK + 수정된 아이템 정보
   */
  @PutMapping("/{id}")
  public ResponseEntity<ItemResponse> updateItem(
      @PathVariable Long id,
      @Valid @RequestBody ItemUpdateRequest request) {
    return ResponseEntity.ok(itemService.updateItem(id, request));
  }

  /**
   * 아이템 삭제.
   * DELETE /api/items/{id}
   *
   * @return 204 No Content
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
    itemService.deleteItem(id);
    return ResponseEntity.noContent().build();
  }
}
