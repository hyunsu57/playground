package com.springjpatest.multithreadservice.controller;

import com.springjpatest.multithreadservice.dto.BulkTaskResult;
import com.springjpatest.multithreadservice.dto.ComparisonResult;
import com.springjpatest.multithreadservice.dto.ScopedValueResult;
import com.springjpatest.multithreadservice.dto.StructuredResult;
import com.springjpatest.multithreadservice.dto.ThreadInfoResult;
import com.springjpatest.multithreadservice.service.ComparisonService;
import com.springjpatest.multithreadservice.service.ScopedValueService;
import com.springjpatest.multithreadservice.service.StructuredConcurrencyService;
import com.springjpatest.multithreadservice.service.VirtualThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Java 21 멀티스레드 기능 데모 REST API.
 *
 * 엔드포인트 목록:
 * GET  /api/multithread/virtual-threads/info        - 가상 스레드 정보 조회
 * POST /api/multithread/virtual-threads/bulk        - 대량 병렬 처리 (기본 1000개)
 * GET  /api/multithread/compare                     - 플랫폼 vs 가상 스레드 성능 비교
 * GET  /api/multithread/structured/all              - Structured Concurrency (전체 성공 대기)
 * GET  /api/multithread/structured/race             - Structured Concurrency (경쟁, 첫 성공 반환)
 * GET  /api/multithread/scoped-values               - Scoped Values 레이어 전파 데모
 */
@RestController
@RequestMapping("/api/multithread")
@RequiredArgsConstructor
public class MultiThreadController {

    private final VirtualThreadService virtualThreadService;
    private final StructuredConcurrencyService structuredConcurrencyService;
    private final ScopedValueService scopedValueService;
    private final ComparisonService comparisonService;

    // ===========================
    // Virtual Threads (JEP 444)
    // ===========================

    /**
     * 현재 요청 스레드와 명시적으로 생성한 가상 스레드 정보를 반환한다.
     * spring.threads.virtual.enabled=true 설정 시 currentThread.isVirtual()=true 확인.
     */
    @GetMapping("/virtual-threads/info")
    public ResponseEntity<Map<String, Object>> getThreadInfo() throws Exception {
        ThreadInfoResult currentThread = virtualThreadService.getCurrentThreadInfo();
        ThreadInfoResult explicitVirtual = virtualThreadService.createAndInspectVirtualThread();

        return ResponseEntity.ok(Map.of(
                "feature", "Virtual Threads - JEP 444 (Java 21 정식)",
                "description", "spring.threads.virtual.enabled=true 로 Tomcat이 가상 스레드 위에서 실행됨",
                "currentRequestThread", currentThread,
                "explicitlyCreatedVirtualThread", explicitVirtual
        ));
    }

    /**
     * count개의 작업을 가상 스레드로 동시 처리한다.
     * I/O 대기 중에도 OS 스레드를 점유하지 않으므로 대량 처리가 가능하다.
     *
     * @param count 처리할 작업 수 (기본 1000)
     */
    @PostMapping("/virtual-threads/bulk")
    public ResponseEntity<BulkTaskResult> processBulk(
            @RequestParam(defaultValue = "1000") int count
    ) throws InterruptedException {
        return ResponseEntity.ok(virtualThreadService.processBulkTasks(count));
    }

    /**
     * 플랫폼 스레드(고정 풀 50개)와 가상 스레드의 처리 시간을 측정·비교한다.
     *
     * @param taskCount     처리할 작업 수 (기본 200)
     * @param simulatedIoMs 작업당 I/O 대기 시간 ms (기본 50)
     */
    @GetMapping("/compare")
    public ResponseEntity<ComparisonResult> compare(
            @RequestParam(defaultValue = "200") int taskCount,
            @RequestParam(defaultValue = "50") int simulatedIoMs
    ) throws InterruptedException {
        return ResponseEntity.ok(comparisonService.compare(taskCount, simulatedIoMs));
    }

    // =====================================
    // Structured Concurrency (JEP 453)
    // =====================================

    /**
     * ShutdownOnFailure: 3개 서비스를 병렬 호출하고 모두 성공해야 반환.
     * 하나라도 실패하면 나머지 즉시 취소 후 예외 전파.
     */
    @GetMapping("/structured/all")
    public ResponseEntity<StructuredResult> structuredAll() throws Exception {
        return ResponseEntity.ok(structuredConcurrencyService.fetchAllParallel());
    }

    /**
     * ShutdownOnSuccess (Hedged Request): 3개 서버에 동시 요청하고
     * 가장 빠른 응답만 반환. 나머지는 즉시 취소.
     */
    @GetMapping("/structured/race")
    public ResponseEntity<StructuredResult> structuredRace() throws Exception {
        return ResponseEntity.ok(structuredConcurrencyService.fetchFirstSuccess());
    }

    // =================================
    // Scoped Values (JEP 446)
    // =================================

    /**
     * ScopedValue로 requestId/userId를 바인딩하고
     * 컨트롤러 → 서비스 → 리포지토리 레이어를 거치며
     * 파라미터 없이 어느 계층에서든 접근 가능함을 보인다.
     *
     * @param requestId 요청 식별자 (기본 "req-001")
     * @param userId    사용자 이메일 (기본 "user@example.com")
     */
    @GetMapping("/scoped-values")
    public ResponseEntity<ScopedValueResult> scopedValues(
            @RequestParam(defaultValue = "req-001") String requestId,
            @RequestParam(defaultValue = "user@example.com") String userId
    ) throws Exception {
        return ResponseEntity.ok(scopedValueService.demonstrate(requestId, userId));
    }
}
