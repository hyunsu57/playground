package com.springjpatest.itemservice.exception;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 전역 예외 처리 핸들러.
 *
 * <p>처리하는 예외 목록:
 * <ul>
 *   <li>{@link MethodArgumentNotValidException}: @RequestBody @Valid 검증 실패 → 400 + 필드별 에러 메시지</li>
 *   <li>{@link BindException}: @ModelAttribute @Valid 검증 실패 → 400 + 필드별 에러 메시지</li>
 *   <li>{@link HttpMessageNotReadableException}: 잘못된 enum 값 등 JSON 파싱 실패 → 400 Bad Request</li>
 *   <li>{@link IllegalArgumentException}: 존재하지 않는 ID 조회 → 404 Not Found</li>
 *   <li>{@link OptimisticLockException}: 동시 수정 충돌 → 409 Conflict</li>
 *   <li>{@link Exception}: 그 외 서버 오류 → 500 Internal Server Error</li>
 * </ul>
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * @RequestBody @Valid 검증 실패 처리 (JSON Body 요청).
   * 검증 실패한 모든 필드와 메시지를 배열로 반환한다.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleRequestBodyValidation(
      MethodArgumentNotValidException ex) {
    return ResponseEntity.badRequest()
        .body(errorBody(HttpStatus.BAD_REQUEST, "입력값 검증 실패", extractFieldErrors(ex)));
  }

  /**
   * @ModelAttribute @Valid 검증 실패 처리 (쿼리 파라미터 요청).
   *
   * <p>{@link MethodArgumentNotValidException}은 {@link BindException}의 하위 타입이므로
   * Spring은 더 구체적인 핸들러({@code handleRequestBodyValidation})를 우선 적용한다.
   * 이 핸들러는 순수 {@link BindException} — 즉 {@code @ModelAttribute} 검증 실패만 처리한다.
   * </p>
   */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleModelAttributeValidation(BindException ex) {
    return ResponseEntity.badRequest()
        .body(errorBody(HttpStatus.BAD_REQUEST, "요청 파라미터 검증 실패", extractFieldErrors(ex)));
  }

  /** BindingResult에서 필드 에러 목록을 추출하는 공통 헬퍼 */
  private List<Map<String, String>> extractFieldErrors(BindException ex) {
    return ex.getBindingResult().getFieldErrors().stream()
        .map(fe -> Map.of("field", fe.getField(), "message", fe.getDefaultMessage()))
        .toList();
  }

  /**
   * JSON 파싱 실패 처리 (잘못된 enum 값, 타입 불일치 등).
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMessageNotReadable(
      HttpMessageNotReadableException ex) {
    return ResponseEntity.badRequest()
        .body(errorBody(HttpStatus.BAD_REQUEST, "요청 본문을 파싱할 수 없습니다. enum 값이나 타입을 확인해주세요.", null));
  }

  /**
   * 존재하지 않는 리소스 요청 처리.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(errorBody(HttpStatus.NOT_FOUND, ex.getMessage(), null));
  }

  /**
   * 동시 수정 충돌 처리.
   * 낙관적 잠금(@Version) 충돌 시 409 Conflict를 반환하여 클라이언트가 재시도하도록 유도한다.
   */
  @ExceptionHandler(OptimisticLockException.class)
  public ResponseEntity<Map<String, Object>> handleOptimisticLock(OptimisticLockException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(errorBody(HttpStatus.CONFLICT,
            "다른 사용자가 동시에 수정 중입니다. 잠시 후 다시 시도해주세요.", null));
  }

  /**
   * 그 외 예상치 못한 서버 오류 처리.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
    return ResponseEntity.internalServerError()
        .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.", null));
  }

  /** 공통 에러 응답 바디 생성 헬퍼 */
  private Map<String, Object> errorBody(HttpStatus status, String message, Object details) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message);
    if (details != null) {
      body.put("details", details);
    }
    return body;
  }
}
