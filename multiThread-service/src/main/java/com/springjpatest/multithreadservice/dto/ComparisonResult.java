package com.springjpatest.multithreadservice.dto;

/**
 * 플랫폼 스레드 vs 가상 스레드 성능 비교 결과 DTO.
 *
 * @param taskCount              총 작업 수
 * @param simulatedIoMs          I/O 대기 시뮬레이션 시간 (ms)
 * @param platformThreadPoolSize 플랫폼 스레드 풀 크기
 * @param platformThreadTimeMs   플랫폼 스레드 총 소요 시간 (ms)
 * @param virtualThreadTimeMs    가상 스레드 총 소요 시간 (ms)
 * @param speedupRatio           속도 향상 배율 (platform / virtual)
 * @param summary                결과 요약 메시지
 */
public record ComparisonResult(
        int taskCount,
        int simulatedIoMs,
        int platformThreadPoolSize,
        long platformThreadTimeMs,
        long virtualThreadTimeMs,
        double speedupRatio,
        String summary
) {}
