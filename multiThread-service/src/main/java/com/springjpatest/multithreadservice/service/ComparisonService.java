package com.springjpatest.multithreadservice.service;

import com.springjpatest.multithreadservice.dto.ComparisonResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/**
 * 플랫폼 스레드 vs 가상 스레드 성능 비교 서비스.
 *
 * 비교 방식:
 * - 플랫폼 스레드: newFixedThreadPool(POOL_SIZE) - 전통적인 방식
 * - 가상 스레드:   newVirtualThreadPerTaskExecutor() - Java 21 방식
 *
 * 기대 결과 (taskCount=200, simulatedIoMs=50 기준):
 * - 플랫폼 (풀 50개): 200/50 * 50ms = ~200ms
 * - 가상 스레드:       모두 동시 실행 → ~50ms
 * - 속도 향상:         약 4배
 *
 * I/O 집약적 워크로드에서 가상 스레드가 효과적임.
 * CPU 집약적 작업(순수 계산)에서는 차이가 없거나 오히려 느릴 수 있음.
 */
@Service
public class ComparisonService {

    private static final int PLATFORM_THREAD_POOL_SIZE = 50;

    /**
     * 동일한 작업을 플랫폼 스레드와 가상 스레드로 각각 실행하여 시간을 측정한다.
     *
     * @param taskCount     처리할 작업 수
     * @param simulatedIoMs 각 작업당 I/O 대기 시간 (ms)
     */
    public ComparisonResult compare(int taskCount, int simulatedIoMs) throws InterruptedException {
        // 플랫폼 스레드 측정 (웜업 효과 제거를 위해 가상 스레드를 나중에 측정)
        long platformTime = measureWithPlatformThreads(taskCount, simulatedIoMs);

        // 가상 스레드 측정
        long virtualTime = measureWithVirtualThreads(taskCount, simulatedIoMs);

        double speedup = virtualTime > 0 ? (double) platformTime / virtualTime : 1.0;

        return new ComparisonResult(
                taskCount,
                simulatedIoMs,
                PLATFORM_THREAD_POOL_SIZE,
                platformTime,
                virtualTime,
                speedup,
                "가상 스레드가 %.1fx 빠름 (작업 %d개, I/O 대기 %dms, 플랫폼 스레드 풀 %d개)"
                        .formatted(speedup, taskCount, simulatedIoMs, PLATFORM_THREAD_POOL_SIZE)
        );
    }

    /**
     * 고정 크기 플랫폼 스레드 풀로 작업 처리.
     * POOL_SIZE보다 많은 작업은 큐에서 대기 → I/O 동안 스레드 블록 → 처리량 제한.
     */
    private long measureWithPlatformThreads(int taskCount, int sleepMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        var latch = new CountDownLatch(taskCount);

        // try-with-resources: Java 19+에서 ExecutorService는 AutoCloseable
        try (var executor = Executors.newFixedThreadPool(PLATFORM_THREAD_POOL_SIZE)) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        // I/O 대기 시뮬레이션
                        // 플랫폼 스레드: 이 동안 OS 스레드를 점유 (다른 작업 실행 불가)
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

    /**
     * 가상 스레드 실행자로 작업 처리.
     * 작업마다 새 가상 스레드 → I/O 동안 캐리어(OS) 스레드 즉시 반환 → 처리량 무제한.
     */
    private long measureWithVirtualThreads(int taskCount, int sleepMs) throws InterruptedException {
        long start = System.currentTimeMillis();
        var latch = new CountDownLatch(taskCount);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        // 가상 스레드: sleep 동안 JVM이 해당 가상 스레드를 언마운트
                        // → 캐리어 스레드가 다른 가상 스레드 실행 가능
                        // → 사실상 모든 taskCount 개 작업이 동시에 실행됨
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
