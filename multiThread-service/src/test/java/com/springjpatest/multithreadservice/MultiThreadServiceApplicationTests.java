package com.springjpatest.multithreadservice;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MultiThreadServiceApplicationTests {

    @Test
    @DisplayName("Spring 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
    }

    @Test
    @DisplayName("Virtual Thread: Thread.isVirtual()이 true를 반환한다")
    void virtualThread_isVirtualShouldBeTrue() throws Exception {
        var future = new java.util.concurrent.CompletableFuture<Boolean>();

        Thread.ofVirtual().start(() -> future.complete(Thread.currentThread().isVirtual()));

        assertThat(future.get()).isTrue();
    }

    @Test
    @DisplayName("Virtual Thread: 1000개 작업을 10ms I/O 대기와 함께 병렬 처리한다")
    void virtualThread_bulk1000TasksShouldCompleteQuickly() throws InterruptedException {
        int taskCount = 1000;
        var latch = new CountDownLatch(taskCount);
        var successCount = new AtomicInteger();

        long start = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(10); // I/O 대기 시뮬레이션
                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        long elapsed = System.currentTimeMillis() - start;

        // 플랫폼 스레드 50개였다면 1000 * 10ms / 50 = ~200ms
        // 가상 스레드는 동시에 실행되므로 ~10~50ms 내 완료 기대
        assertThat(successCount.get()).isEqualTo(taskCount);
        assertThat(elapsed).isLessThan(5000L); // 5초 이내
    }

    @Test
    @DisplayName("Virtual Thread: 플랫폼 스레드보다 I/O 바운드 처리가 빠르다")
    void virtualThread_shouldBeFasterThanPlatformThreadForIoBound() throws InterruptedException {
        int taskCount = 100;
        int sleepMs = 50;

        // 플랫폼 스레드 (풀 10개)
        long platformTime = measureTime(taskCount, sleepMs, false);

        // 가상 스레드
        long virtualTime = measureTime(taskCount, sleepMs, true);

        // 가상 스레드가 더 빨라야 함
        assertThat(virtualTime).isLessThan(platformTime);
    }

    private long measureTime(int taskCount, int sleepMs, boolean useVirtual) throws InterruptedException {
        var latch = new CountDownLatch(taskCount);
        long start = System.currentTimeMillis();

        try (var executor = useVirtual
                ? Executors.newVirtualThreadPerTaskExecutor()
                : Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
        }

        return System.currentTimeMillis() - start;
    }
}
