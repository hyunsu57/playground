package com.springjpatest.itemservice.dto;

import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 아이템 응답 DTO (record).
 *
 * <p>엔티티를 직접 노출하지 않고 필요한 필드만 반환한다.
 * {@link #from(Item)} 팩토리 메서드로 엔티티에서 변환한다.
 * </p>
 */
public record ItemResponse(
    Long id,
    String server,
    String sellerName,
    ItemConst.ItemType itemType,
    String title,
    BigDecimal price,
    int quantity,
    LocalDateTime createdAt
) {

  /**
   * 아이템 엔티티를 응답 DTO로 변환한다.
   *
   * @param item 변환할 아이템 엔티티
   * @return 응답 DTO
   */
  public static ItemResponse from(Item item) {
    return new ItemResponse(
        item.getId(),
        item.getServer(),
        item.getSellerName(),
        item.getItemType(),
        item.getTitle(),
        item.getPrice(),
        item.getQuantity(),
        item.getCreatedAt()
    );
  }
}
