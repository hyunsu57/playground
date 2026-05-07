package com.springjpatest.multithreadservice.dto;

import java.util.List;

/**
 * Scoped Values 데모 결과 DTO.
 *
 * @param requestId   바인딩된 요청 ID
 * @param userId      바인딩된 사용자 ID
 * @param callChain   레이어별 ScopedValue 접근 로그 (컨트롤러 → 서비스 → 리포지토리)
 * @param explanation ScopedValue vs ThreadLocal 비교 설명
 */
public record ScopedValueResult(
        String requestId,
        String userId,
        List<String> callChain,
        String explanation
) {}
