package com.springjpatest.multithreadservice.dto;

import java.util.List;

/**
 * 대량 병렬 작업 처리 결과 DTO.
 *
 * @param totalTasks       총 작업 수
 * @param successCount     성공한 작업 수
 * @param totalTimeMs      전체 소요 시간 (ms)
 * @param avgTimePerTaskMs 작업당 평균 시간 (ms)
 * @param sampleThreadNames 처음 5개 작업을 실행한 스레드 이름 (가상 스레드 이름 확인용)
 */
public record BulkTaskResult(
        int totalTasks,
        int successCount,
        long totalTimeMs,
        double avgTimePerTaskMs,
        List<String> sampleThreadNames
) {}
