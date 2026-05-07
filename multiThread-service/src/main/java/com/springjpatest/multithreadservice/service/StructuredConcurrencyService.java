package com.springjpatest.multithreadservice.service;

import com.springjpatest.multithreadservice.dto.StructuredResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/**
 * Structured Concurrency (JEP 453, Java 21 프리뷰) 데모 서비스.
 *
 * 핵심 개념:
 * Structured Concurrency는 동시 작업의 생명주기를 "구조화"한다.
 * 스코프(try 블록) 안에서 시작된 작업(fork)은 스코프가 닫힐 때까지 반드시 완료된다.
 * → 작업 누수(task leak) 방지, 오류 전파 일관성 확보
 *
 * 기존 CompletableFuture의 문제:
 * - 작업 취소/오류 전파 로직을 개발자가 직접 구현해야 함
 * - 작업이 어느 스레드에서 언제 완료되는지 추적 어려움
 *
 * StructuredTaskScope 전략:
 * - ShutdownOnFailure: 하나라도 실패하면 나머지 즉시 취소 (모두 성공해야 반환)
 * - ShutdownOnSuccess: 첫 번째 성공 즉시 나머지 취소 (Hedged Request 패턴)
 *
 * 컴파일/실행 시 --enable-preview 필요 (build.gradle에 설정됨)
 */
@Service
public class StructuredConcurrencyService {

    /**
     * ShutdownOnFailure 전략.
     * 3개 외부 서비스를 병렬 호출하고 모두 성공해야 결과를 반환한다.
     * 하나라도 실패하면 나머지 작업이 즉시 취소되고 예외가 전파된다.
     *
     * 사용 사례: 주문 처리 시 재고/결제/배송 서비스 모두 확인
     */
    public StructuredResult fetchAllParallel() throws Exception {
        long start = System.currentTimeMillis();

        // try-with-resources: 스코프 닫힐 때 미완료 작업 자동 취소
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // fork(): 각 작업을 새 가상 스레드에서 비동기 실행
            var taskBlog   = scope.fork(() -> simulateFetch("blog-service",   200));
            var taskItem   = scope.fork(() -> simulateFetch("item-service",   150));
            var taskAuth   = scope.fork(() -> simulateFetch("auth-service",   100));

            // join(): 모든 fork 완료(또는 하나 실패) 시까지 대기
            // throwIfFailed(): 실패한 fork가 있으면 예외 발생 → 나머지 작업 취소
            scope.join().throwIfFailed();

            // join() 이후에만 .get() 호출 가능 (그 전엔 미완료 상태)
            return new StructuredResult(
                    "ShutdownOnFailure",
                    List.of(taskBlog.get(), taskItem.get(), taskAuth.get()),
                    System.currentTimeMillis() - start,
                    "3개 서비스를 병렬 호출. 모두 성공해야 반환. 하나 실패 시 전체 취소. " +
                    "순차 처리(200+150+100=450ms)가 아닌 병렬 처리(~200ms) 확인."
            );
        }
    }

    /**
     * ShutdownOnSuccess 전략 (Hedged Request 패턴).
     * 동일한 작업을 여러 서버에 동시 요청하고, 가장 먼저 성공한 결과만 반환한다.
     * 나머지는 즉시 취소 → 불필요한 대기 없음.
     *
     * 사용 사례: CDN 다중 서버에서 가장 빠른 응답 선택, 다중 DB 읽기 복제본 중 빠른 것 선택
     */
    public StructuredResult fetchFirstSuccess() throws Exception {
        long start = System.currentTimeMillis();

        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {

            // 세 서버에 동시 요청 (느린 것 800ms, 중간 300ms, 빠른 것 100ms)
            scope.fork(() -> simulateFetch("느린-서버 (800ms)",  800));
            scope.fork(() -> simulateFetch("중간-서버 (300ms)", 300));
            scope.fork(() -> simulateFetch("빠른-서버 (100ms)", 100));

            // 첫 번째 성공 즉시 반환 → 나머지 두 작업 취소
            // 전체 소요 시간이 ~100ms임을 확인 (가장 빠른 서버 기준)
            scope.join();

            return new StructuredResult(
                    "ShutdownOnSuccess (Hedged Request)",
                    List.of(scope.result()),
                    System.currentTimeMillis() - start,
                    "3개 서버에 동시 요청. 가장 빠른 응답(~100ms)만 채택. 나머지 즉시 취소. " +
                    "응답 시간이 최선의 서버 기준으로 단축됨."
            );
        }
    }

    /** 외부 서비스 HTTP 호출을 시뮬레이션 (Thread.sleep으로 대체). */
    private String simulateFetch(String serviceName, long delayMs) throws InterruptedException {
        Thread.sleep(delayMs);
        return "%s 응답 완료 (스레드: %s, %dms 소요)".formatted(
                serviceName,
                Thread.currentThread().getName(),
                delayMs
        );
    }
}
