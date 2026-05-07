package com.springjpatest.multithreadservice.dto;

/**
 * 스레드 정보 응답 DTO.
 * Java 16+에서 정식화된 record를 사용한다.
 *
 * @param threadName   스레드 이름
 * @param isVirtual    가상 스레드 여부 (Thread.isVirtual() - Java 21 신규)
 * @param isDaemon     데몬 스레드 여부
 * @param threadId     스레드 ID
 * @param state        스레드 상태 (RUNNABLE, BLOCKED, WAITING 등)
 */
public record ThreadInfoResult(
        String threadName,
        boolean isVirtual,
        boolean isDaemon,
        long threadId,
        String state
) {
    /** 현재 실행 중인 스레드의 정보를 담은 인스턴스를 생성한다. */
    public static ThreadInfoResult fromCurrentThread() {
        Thread t = Thread.currentThread();
        return new ThreadInfoResult(
                t.getName(),
                t.isVirtual(),
                t.isDaemon(),
                t.threadId(),
                t.getState().name()
        );
    }
}
