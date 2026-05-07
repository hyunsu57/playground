package com.springjpatest.multithreadservice.dto;

import java.util.List;

/**
 * Structured Concurrency 실행 결과 DTO.
 *
 * @param strategy    사용된 전략 (ShutdownOnFailure / ShutdownOnSuccess)
 * @param results     각 포크 작업의 결과 목록
 * @param elapsedMs   전체 소요 시간 (ms)
 * @param description 전략 설명
 */
public record StructuredResult(
        String strategy,
        List<String> results,
        long elapsedMs,
        String description
) {}
