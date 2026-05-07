package com.springjpatest.multithreadservice.service;

import com.springjpatest.multithreadservice.dto.ScopedValueResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Scoped Values (JEP 446, Java 21 프리뷰) 데모 서비스.
 *
 * 핵심 개념:
 * ScopedValue는 메서드 파라미터로 전달하지 않아도 특정 스코프 내 모든 호출 계층에서
 * 접근 가능한 불변 값이다. ThreadLocal의 가상 스레드 친화적 대안.
 *
 * ThreadLocal vs ScopedValue:
 * ┌──────────────────┬────────────────────────────┬──────────────────────────────┐
 * │ 특성              │ ThreadLocal                 │ ScopedValue                  │
 * ├──────────────────┼────────────────────────────┼──────────────────────────────┤
 * │ 변경 가능성       │ set()으로 언제든 변경 가능  │ 스코프 내에서 불변           │
 * │ 가상 스레드 지원  │ 수백만 개 시 메모리 부담    │ 가상 스레드에 최적화         │
 * │ 스코프 관리       │ remove() 수동 호출 필요     │ 스코프 종료 시 자동 해제     │
 * │ 성능              │ ThreadLocal 저장소 접근     │ 직접 접근 (빠름)             │
 * └──────────────────┴────────────────────────────┴──────────────────────────────┘
 *
 * 사용 사례: 요청 ID, 사용자 컨텍스트, 트레이싱 정보를 계층 간 명시적 전달 없이 공유
 *
 * 컴파일/실행 시 --enable-preview 필요 (build.gradle에 설정됨)
 */
@Service
public class ScopedValueService {

    // ScopedValue는 static final 상수로 선언 (ThreadLocal과 동일한 패턴)
    // 값은 나중에 ScopedValue.where()로 바인딩
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> USER_ID    = ScopedValue.newInstance();

    /**
     * ScopedValue로 requestId와 userId를 바인딩하고
     * 컨트롤러 → 서비스 → 리포지토리 레이어 전반에서 명시적 파라미터 없이 접근한다.
     *
     * @param requestId HTTP 요청 ID
     * @param userId    사용자 이메일
     */
    public ScopedValueResult demonstrate(String requestId, String userId) throws Exception {
        List<String> callChain = new ArrayList<>();

        // ScopedValue.where().where().call():
        // 지정한 스코프(람다) 안에서만 값이 유효하고, 스코프 종료 시 자동 해제
        return ScopedValue
                .where(REQUEST_ID, requestId)
                .where(USER_ID, userId)
                .call(() -> {
                    // 레이어1 (현재 위치) - 파라미터 없이 접근
                    callChain.add("1. [ScopedValueService] REQUEST_ID=%s, USER_ID=%s"
                            .formatted(REQUEST_ID.get(), USER_ID.get()));

                    // 레이어2 호출 - 파라미터로 전달하지 않아도 접근 가능
                    businessLogicLayer(callChain);

                    // 레이어3 호출
                    dataAccessLayer(callChain);

                    // 자식 가상 스레드에서도 접근 가능 여부 확인
                    verifyChildThreadAccess(callChain);

                    return new ScopedValueResult(
                            REQUEST_ID.get(),
                            USER_ID.get(),
                            callChain,
                            "ScopedValue는 ThreadLocal과 달리 스코프 내에서 불변(immutable)이며, " +
                            "가상 스레드와 자식 스코프에 자동으로 상속됩니다. " +
                            "명시적 파라미터 전달 없이 REQUEST_ID가 모든 레이어에서 접근되었습니다."
                    );
                });
    }

    /** 비즈니스 로직 레이어 - REQUEST_ID를 파라미터로 받지 않아도 접근 가능. */
    private void businessLogicLayer(List<String> callChain) {
        callChain.add("2. [BusinessLogicLayer] REQUEST_ID=%s 접근 성공 (파라미터 전달 없음)"
                .formatted(REQUEST_ID.get()));
    }

    /** 데이터 접근 레이어 - USER_ID를 파라미터로 받지 않아도 접근 가능. */
    private void dataAccessLayer(List<String> callChain) {
        callChain.add("3. [DataAccessLayer] USER_ID=%s 접근 성공 (파라미터 전달 없음)"
                .formatted(USER_ID.get()));
    }

    /**
     * 자식 가상 스레드에서 ScopedValue 접근을 검증한다.
     * Structured Concurrency와 함께 사용 시 자식 스코프에 자동 상속된다.
     */
    private void verifyChildThreadAccess(List<String> callChain) throws Exception {
        var future = new java.util.concurrent.CompletableFuture<String>();

        Thread.ofVirtual()
                .name("child-virtual-thread")
                .start(() -> {
                    try {
                        // 부모 스코프에서 바인딩된 값이 자식 가상 스레드에서도 접근 가능
                        String val = REQUEST_ID.get();
                        future.complete("4. [자식 가상 스레드] REQUEST_ID=%s 접근 성공".formatted(val));
                    } catch (Exception e) {
                        future.complete("4. [자식 가상 스레드] 접근 실패: " + e.getMessage());
                    }
                });

        callChain.add(future.get(5, java.util.concurrent.TimeUnit.SECONDS));
    }
}
