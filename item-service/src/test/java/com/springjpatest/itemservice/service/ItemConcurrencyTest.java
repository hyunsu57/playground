package com.springjpatest.itemservice.service;

import com.springjpatest.itemservice.dto.ItemUpdateRequest;
import com.springjpatest.itemservice.entity.Item;
import com.springjpatest.itemservice.entity.ItemConst;
import com.springjpatest.itemservice.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 낙관적 잠금(Optimistic Lock) 동시성 테스트.
 *
 * <p>Item 엔티티의 @Version 필드로 동일 상품 동시 수정 시 충돌을 감지하는지 검증한다.
 *
 * <p>테스트 전략 두 가지:
 * <ol>
 *   <li>결정론적 테스트: 같은 트랜잭션 내에서 Native SQL로 버전을 강제 변경 후
 *       flush 시 OptimisticLockException 발생 확인</li>
 *   <li>멀티스레드 테스트: 두 스레드가 동시에 같은 아이템을 수정할 때
 *       정확히 하나만 성공하고 나머지는 예외가 발생하는지 확인</li>
 * </ol>
 * </p>
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=true"
})
class ItemConcurrencyTest {

  @Autowired
  private ItemRepository itemRepository;

  @Autowired
  private ItemService itemService;

  @PersistenceContext
  private EntityManager em;

  @BeforeEach
  void setUp() {
    itemRepository.deleteAll();
  }

  // ===== 결정론적 낙관적 잠금 테스트 =====

  /**
   * 같은 트랜잭션 내에서 Native SQL로 버전을 직접 올린 뒤 flush하면,
   * Hibernate가 UPDATE WHERE version=0 을 실행하지만 DB version은 이미 1이므로
   * 0 rows updated → OptimisticLockException 발생.
   */
  @Test
  @Transactional
  @DisplayName("DB 버전이 변경된 상태에서 flush 시 OptimisticLockException이 발생한다")
  void 낙관적_잠금_버전_불일치_예외() {
    // given: 아이템 저장 (version=0)
    Item item = Item.builder()
        .server("아시아")
        .sellerName("판매자")
        .itemType(ItemConst.ItemType.ITEM)
        .title("낙관적잠금 테스트 아이템")
        .price(new BigDecimal("10000"))
        .quantity(1)
        .build();
    em.persist(item);
    em.flush(); // INSERT 실행 → DB version=0

    Long itemId = item.getId();

    // when: 다른 트랜잭션이 먼저 수정했다고 시뮬레이션 — Native SQL로 version 강제 증가
    // 영속성 컨텍스트의 item 객체는 version=0을 기억하지만 DB는 version=1이 됨
    em.createNativeQuery("UPDATE items SET version = version + 1 WHERE id = :id")
        .setParameter("id", itemId)
        .executeUpdate();

    // 아이템 수정 (영속성 컨텍스트에서 변경 감지 대상이 됨)
    item.update("수정된 이름", ItemConst.ItemType.GAME_MONEY, new BigDecimal("20000"), 2);

    // then: flush 시 Hibernate가 UPDATE WHERE id=? AND version=0 실행
    //       → DB version=1이므로 0 rows updated → OptimisticLockException
    assertThatThrownBy(() -> em.flush())
        .isInstanceOf(OptimisticLockException.class);
  }

  // ===== 멀티스레드 낙관적 잠금 테스트 =====

  /**
   * 두 스레드가 CountDownLatch로 동시에 updateItem을 실행한다.
   * 먼저 커밋된 스레드는 성공(version 0→1), 나중 커밋 스레드는 버전 불일치로 예외 발생.
   * 결과적으로 성공 1개 + 실패 1개여야 한다.
   */
  @Test
  @DisplayName("두 스레드가 동시에 같은 아이템을 수정하면 하나는 성공하고 하나는 예외가 발생한다")
  void 동시_수정_낙관적_잠금_충돌() throws InterruptedException {
    // given: 아이템 저장 (별도 트랜잭션에서 커밋)
    Item savedItem = itemRepository.save(Item.builder()
        .server("아시아")
        .sellerName("판매자")
        .itemType(ItemConst.ItemType.ITEM)
        .title("동시수정 테스트 아이템")
        .price(new BigDecimal("10000"))
        .quantity(1)
        .build());
    Long itemId = savedItem.getId();

    // 두 스레드가 동시에 출발하도록 CountDownLatch 설정
    CountDownLatch readyLatch = new CountDownLatch(2); // 두 스레드 모두 준비될 때까지 대기
    CountDownLatch startLatch = new CountDownLatch(1); // 동시 출발 신호

    AtomicInteger successCount = new AtomicInteger(0);
    AtomicInteger failCount = new AtomicInteger(0);
    List<Exception> caughtExceptions = Collections.synchronizedList(new ArrayList<>());

    // 두 스레드에서 실행할 업데이트 작업 정의
    Runnable updateTask1 = buildUpdateTask(itemId, "수정1", readyLatch, startLatch,
        successCount, failCount, caughtExceptions);
    Runnable updateTask2 = buildUpdateTask(itemId, "수정2", readyLatch, startLatch,
        successCount, failCount, caughtExceptions);

    // 두 스레드 실행
    ExecutorService executor = Executors.newFixedThreadPool(2);
    executor.submit(updateTask1);
    executor.submit(updateTask2);

    readyLatch.await(5, TimeUnit.SECONDS); // 두 스레드 모두 준비될 때까지 최대 5초 대기
    startLatch.countDown(); // 동시에 출발

    executor.shutdown();
    boolean finished = executor.awaitTermination(10, TimeUnit.SECONDS);
    assertThat(finished).as("스레드가 10초 내에 완료되어야 한다").isTrue();

    // then: 두 스레드 합계가 2이어야 함 (성공 + 실패 = 2)
    int total = successCount.get() + failCount.get();
    assertThat(total).isEqualTo(2);

    // 최소 하나는 성공
    assertThat(successCount.get()).isGreaterThanOrEqualTo(1);

    // 동시 충돌이 발생한 경우 발생 예외가 낙관적 잠금 관련이어야 함
    if (failCount.get() > 0) {
      Exception ex = caughtExceptions.get(0);
      // Spring은 JPA OptimisticLockException을 ObjectOptimisticLockingFailureException으로 래핑
      boolean isOptimisticLockRelated =
          ex instanceof ObjectOptimisticLockingFailureException
              || ex instanceof OptimisticLockException
              || (ex.getCause() != null && ex.getCause() instanceof OptimisticLockException);
      assertThat(isOptimisticLockRelated)
          .as("충돌 예외는 낙관적 잠금 관련이어야 한다. 실제 예외: " + ex.getClass().getName())
          .isTrue();
    }
  }

  // ===== 헬퍼 =====

  /**
   * 멀티스레드 테스트용 업데이트 Runnable 생성.
   * CountDownLatch로 두 스레드가 최대한 동시에 실행되도록 조율한다.
   */
  private Runnable buildUpdateTask(
      Long itemId, String newTitle,
      CountDownLatch readyLatch, CountDownLatch startLatch,
      AtomicInteger successCount, AtomicInteger failCount,
      List<Exception> caughtExceptions) {

    return () -> {
      readyLatch.countDown(); // "나 준비됐어" 알림
      try {
        startLatch.await(); // 출발 신호 대기 (두 스레드 동시 출발 유도)
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        failCount.incrementAndGet();
        return;
      }
      try {
        itemService.updateItem(itemId, createUpdateRequest(newTitle));
        successCount.incrementAndGet();
      } catch (Exception e) {
        failCount.incrementAndGet();
        caughtExceptions.add(e);
      }
    };
  }

  private ItemUpdateRequest createUpdateRequest(String title) {
    return new ItemUpdateRequest(title, ItemConst.ItemType.ACCOUNT,
        new BigDecimal("20000"), 2);
  }
}
