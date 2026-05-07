package com.springjpatest.multithreadservice.service;

import com.springjpatest.multithreadservice.dto.BulkTaskResult;
import com.springjpatest.multithreadservice.dto.ThreadInfoResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Virtual Threads (JEP 444, Java 21 정식) 데모 서비스.
 *
 * 핵심 개념:
 * - Thread.ofVirtual(): 가상 스레드 빌더
 * - Executors.newVirtualThreadPerTaskExecutor(): 태스크마다 가상 스레드를 생성하는 ExecutorService
 * - Thread.isVirtual(): 현재 스레드가 가상 스레드인지 확인
 * - ExecutorService implements AutoCloseable (Java 19+): try-with-resources 사용 가능
 *
 * 가상 스레드 특성:
 * - OS 스레드와 1:1 매핑이 아닌 JVM 관리 경량 스레드
 * - 블로킹 I/O 시 캐리어(OS) 스레드를 즉시 반환 → OS 스레드 낭비 없음
 * - 스택 크기가 동적으로 성장/축소 → 수백만 개 동시 생성 가능
 * - ThreadLocal 지원하지만 풀링은 안티패턴 (매번 새로 생성)
 */
@Service
public class VirtualThreadService {

    /**
     * HTTP 요청을 처리하는 현재 스레드 정보를 반환한다.
     * application.yml의 spring.threads.virtual.enabled=true 설정 시
     * Tomcat이 가상 스레드 위에서 요청을 처리하므로 isVirtual=true가 된다.
     */
    public ThreadInfoResult getCurrentThreadInfo() {
        return ThreadInfoResult.fromCurrentThread();
    }

    /**
     * Thread.ofVirtual() 빌더로 명시적으로 가상 스레드를 생성하고
     * 그 스레드 자체의 정보를 반환한다.
     */
    public ThreadInfoResult createAndInspectVirtualThread() throws Exception {
        var future = new CompletableFuture<ThreadInfoResult>();

        // Thread.ofVirtual(): 가상 스레드 빌더 (Java 21)
        Thread.ofVirtual()
                .name("explicit-virtual-thread")
                .start(() -> future.complete(ThreadInfoResult.fromCurrentThread()));

        return future.get(5, TimeUnit.SECONDS);
    }

    /**
     * taskCount 개의 작업을 가상 스레드 풀로 병렬 처리한다.
     * 각 작업은 10ms I/O 대기를 시뮬레이션한다.
     *
     * 핵심 관찰 포인트:
     * - taskCount=1000 이더라도 전체 소요 시간이 ~10ms 수준 (모두 동시 실행)
     * - 플랫폼 스레드 고정 풀(50개)이었다면 1000 * 10ms / 50 = ~200ms
     *
     * @param taskCount 처리할 작업 수 (기본값 1000)
     */
    public BulkTaskResult processBulkTasks(int taskCount) throws InterruptedException {
        long start = System.currentTimeMillis();
        var threadNames = Collections.synchronizedList(new ArrayList<String>());
        var successCount = new AtomicInteger(0);
        var latch = new CountDownLatch(taskCount);

        // try-with-resources: ExecutorService.close()가 모든 작업 완료까지 대기
        // newVirtualThreadPerTaskExecutor(): 각 submit마다 새 가상 스레드 생성
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    try {
                        // 블로킹 I/O 시뮬레이션
                        // 플랫폼 스레드: sleep 동안 OS 스레드 점유
                        // 가상 스레드: sleep 동안 OS 스레드 즉시 반환 (언마운트) → 다른 가상 스레드 실행
                        Thread.sleep(10);
                        threadNames.add(Thread.currentThread().getName());
                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(30, TimeUnit.SECONDS);
        }

        long elapsed = System.currentTimeMillis() - start;
        List<String> sample = threadNames.stream().limit(5).toList();

        return new BulkTaskResult(
                taskCount,
                successCount.get(),
                elapsed,
                (double) elapsed / taskCount,
                sample
        );
    }
}
