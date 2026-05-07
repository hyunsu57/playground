package com.springjpatest.itemservice.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 아이템 엔티티.
 *
 * <p>H2(개발) / PostgreSQL(운영)에 저장되는 게임 아이템 거래 상품 정보.
 * {@code @Version}으로 낙관적 잠금을 적용하여 동일 상품 동시 수정 시
 * {@link jakarta.persistence.OptimisticLockException}이 발생한다.
 * </p>
 */
@Entity
@Table(name = "items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 (외부 직접 생성 방지)
@EntityListeners(AuditingEntityListener.class)     // @CreatedDate 활성화
public class Item {

  /** 상품 고유 식별자 (자동 증가) */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 낙관적 잠금 버전 필드.
   *
   * <p>동일 상품을 두 트랜잭션이 동시에 수정할 때,
   * 먼저 커밋된 트랜잭션이 버전을 올리면 나중 트랜잭션에서 충돌을 감지한다.
   * </p>
   */
  @Version
  private Long version;

  /** 서버 (예: "라엘08", "오르페 통합거래소") */
  @Column(nullable = false, length = 50)
  private String server;

  /** 판매자 이름 */
  @Column(nullable = false, length = 50)
  private String sellerName;

  /** 상품 종류 (enum: GAME_MONEY, ITEM, ACCOUNT, ETC) */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ItemConst.ItemType itemType;

  /** 상품명 */
  @Column(nullable = false, length = 100)
  private String title;

  /** 거래 가격 (원 단위, 양수만 허용) */
  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal price;

  /** 판매 수량 (양수만 허용) */
  @Column(nullable = false)
  private int quantity;

  /** 등록 시각 (자동 설정, 수정 불가) */
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  /**
   * 아이템 생성 팩토리 메서드.
   * Builder 패턴을 통해 불변 생성을 유도한다.
   */
  @Builder
  public Item(String server, String sellerName, ItemConst.ItemType itemType,
      String title, BigDecimal price, int quantity) {
    this.server = server;
    this.sellerName = sellerName;
    this.itemType = itemType;
    this.title = title;
    this.price = price;
    this.quantity = quantity;
  }

  /**
   * 아이템 정보를 수정한다.
   * 수정 가능한 필드: 제목, 상품 종류, 가격, 판매 수량.
   *
   * @param title    수정할 상품명
   * @param itemType 수정할 상품 종류
   * @param price    수정할 가격
   * @param quantity 수정할 수량
   */
  public void update(String title, ItemConst.ItemType itemType, BigDecimal price, int quantity) {
    this.title = title;
    this.itemType = itemType;
    this.price = price;
    this.quantity = quantity;
  }
}
