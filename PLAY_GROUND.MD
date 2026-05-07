# playGround 프로젝트 구조 분석

Spring 기술 블로그 플랫폼 — Spring 학습 내용을 설명하고 기능을 직접 체험할 수 있는 MSA 기반 프로젝트.

---

## 전체 아키텍처

```
Client (React 3000)
        │
        ▼
Gateway Service (8080)  ← JWT 1차 검증, 라우팅, CORS
        │
   ┌────┼────┬────────┬────────────┐
   ▼    ▼    ▼        ▼            ▼
blog  auth  item   api-svc     websocket
8081  8082  8084   8083         8085
 │     │     │
PostgreSQL (prod) / H2 (dev)
```

---

## 기술 스택

| 영역 | 기술 |
|------|------|
| 백엔드 | Spring Boot 4.0.3, Spring Cloud 2025.1.0, Java 21 |
| 프론트엔드 | React 19, Vite 6, React Router v7, React Query v5, Axios |
| 인증 | JWT (jjwt 0.12.6), BCrypt |
| 데이터베이스 | H2 (dev 프로파일) / PostgreSQL 16 (prod 프로파일) |
| 캐싱 | Caffeine (item-service) |
| 실시간 | STOMP over WebSocket (SockJS 폴백) |
| 빌드 | Gradle 멀티모듈 |
| 배포 | Docker Compose (로컬), Render.com (운영) |

---

## 멀티모듈 구조

```
playGround/
├── settings.gradle               # 모듈 등록
├── build.gradle                  # 공통 의존성 (Lombok, JUnit)
├── docker-compose.yml
├── gateway-service/              # 포트 8080
├── blog-service/                 # 포트 8081
├── auth-service/                 # 포트 8082
├── api-service/                  # 포트 8083 (미구현)
├── item-service/                 # 포트 8084
├── websocket-service/            # 포트 8085
└── frontend/                     # 포트 3000
```

**루트 build.gradle**: Spring Boot 4.0.3 + Dependency Management 플러그인 모든 서브모듈에 `apply false`로 선언, Java 21 컴파일 타겟, Spring Cloud 2025.1.0 BOM 통합 관리.

---

## 1. Gateway Service (8080)

### 역할
- 단일 진입점: 모든 클라이언트 요청 수신
- JWT 1차 검증 (서명 + 만료)
- 하위 서비스로 라우팅
- 전역 CORS 처리

### 의존성
- Spring Cloud Gateway (WebFlux 기반)
- jjwt 0.12.6

### 라우팅 규칙 (application.yml)

| 경로 패턴 | 대상 서비스 |
|-----------|------------|
| `/api/posts/**`, `/api/categories/**` | blog-service:8081 |
| `/api/auth/**` | auth-service:8082 |
| `/api/external/**` | api-service:8083 |
| `/api/items/**` | item-service:8084 |
| `/api/chat/**`, `/ws/**` | websocket-service:8085 |

### JWT 필터 (`JwtAuthFilter.java`)
- **화이트리스트** (검증 생략): `/api/auth/login`, `/api/auth/signup`, `/api/auth/refresh`
- 검증 성공: `X-User-Email` 헤더로 사용자 이메일 하위 서비스 전달
- 검증 실패: `401 Unauthorized` 반환

### 패키지
```
com.springjpatest.gateway
├── GatewayServiceApplication.java
├── filter/
│   └── JwtAuthFilter.java
└── config/
    └── CorsConfig.java
```

---

## 2. Blog Service (8081)

### 역할
- 게시글 CRUD REST API
- Spring Web MVC + JPA

### 엔티티 (`Post.java`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK, 자동증가 |
| title | String | 제목 (최대 200자) |
| content | String | 본문 (TEXT, 마크다운) |
| summary | String | 요약 (최대 500자) |
| authorEmail | String | 작성자 이메일 |
| published | boolean | 공개 여부 |
| createdAt | LocalDateTime | @CreatedDate |
| updatedAt | LocalDateTime | @LastModifiedDate |

### API 엔드포인트

| 메서드 | 경로 | 설명 | 인증 |
|--------|------|------|------|
| GET | `/api/posts` | 공개 게시글 목록 (페이지네이션) | 불필요 |
| GET | `/api/posts/{id}` | 게시글 단건 조회 | 불필요 |
| POST | `/api/posts` | 게시글 생성 | 필요 |
| PUT | `/api/posts/{id}` | 게시글 수정 (작성자만) | 필요 |
| DELETE | `/api/posts/{id}` | 게시글 삭제 (작성자만) | 필요 |

### 비즈니스 규칙
- 작성자 권한 검증: Gateway에서 전달받은 `X-User-Email` vs `authorEmail` 비교
- `@Transactional(readOnly = true)` 기본 적용 (성능 최적화)

### DB 설정
- **dev**: H2 인메모리, `ddl-auto: create-drop`, `/h2-console` 활성화
- **prod**: PostgreSQL, `ddl-auto: validate`

### 패키지
```
com.springjpatest.blog
├── BlogServiceApplication.java
├── entity/Post.java
├── dto/PostRequest.java, PostResponse.java
├── repository/PostRepository.java
├── service/PostService.java
├── controller/PostController.java
└── config/SecurityConfig.java
```

---

## 3. Auth Service (8082)

### 역할
- 회원가입 / 로그인 / 토큰 재발급
- JWT 토큰 발급 (액세스 + 리프레시)

### 엔티티 (`User.java`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| email | String | UNIQUE, 로그인 ID |
| password | String | BCrypt 해시 |
| nickname | String | 닉네임 |
| role | Role enum | ROLE_USER / ROLE_ADMIN |
| createdAt | LocalDateTime | @CreatedDate |

### JWT 토큰

| 토큰 | 유효기간 | Subject |
|------|---------|---------|
| 액세스 토큰 | 1시간 | 사용자 이메일 |
| 리프레시 토큰 | 7일 | 사용자 이메일 |

서명 알고리즘: HMAC-SHA256 (256비트 이상 비밀 키)

### API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | 토큰 재발급 |

### DTO
- `SignupRequest`: email, password (최소 8자), nickname (2~20자)
- `LoginRequest`: email, password
- `TokenResponse`: accessToken, refreshToken, tokenType ("Bearer")

### 패키지
```
com.springjpatest.auth
├── AuthServiceApplication.java
├── entity/User.java, Role.java
├── dto/SignupRequest.java, LoginRequest.java, TokenResponse.java
├── repository/UserRepository.java
├── service/AuthService.java
├── controller/AuthController.java
├── security/JwtTokenProvider.java
└── config/SecurityConfig.java
```

---

## 4. Item Service (8084)

### 역할
- 게임 아이템 거래 목록 관리
- 캐싱(Caffeine) + 낙관적 잠금(@Version)

### 엔티티 (`Item.java`)

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Long | PK |
| version | Long | @Version (낙관적 잠금) |
| server | String | 서버명 (예: "라엘08") |
| sellerName | String | 판매자명 |
| itemType | ItemType enum | GAME_MONEY, ITEM, ACCOUNT, ETC, ALL |
| title | String | 상품명 (최대 100자) |
| price | BigDecimal | 거래 가격 |
| quantity | int | 판매 수량 |
| createdAt | LocalDateTime | @CreatedDate |

### API 엔드포인트

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/items` | 목록 조회 (필터링, 페이지네이션, 캐싱) |
| GET | `/api/items/{id}` | 단건 조회 |
| POST | `/api/items` | 등록 |
| PUT | `/api/items/{id}` | 수정 (낙관적 잠금) |
| DELETE | `/api/items/{id}` | 삭제 |

### 조회 파라미터 (`ItemListRequest`)
- keyword (LIKE 검색), itemType, server, minPrice, maxPrice
- page, size, sort (RegDateMin/RegDateMax/PriceMin/PriceMax)

### 정렬 옵션 (`ItemConst.ItemSort`)

| 값 | 설명 |
|----|------|
| RegDateMin | 오래된 순 (ASC) |
| RegDateMax | 최신 순 (DESC, 기본값) |
| PriceMin | 높은 가격 순 (DESC) |
| PriceMax | 낮은 가격 순 (ASC) |

### 캐싱 전략
- `@Cacheable`: 목록 조회 파라미터 조합별 캐싱
- `@CacheEvict(allEntries=true)`: 생성/수정/삭제 시 전체 캐시 무효화
- **dev**: 최대 200개, TTL 10분
- **prod**: 최대 500개, TTL 5분

### 낙관적 잠금
- 동시 수정 충돌 시 `OptimisticLockException` → `409 Conflict` 반환

### 예외 처리 (`GlobalExceptionHandler`)

| HTTP 상태 | 원인 |
|-----------|------|
| 400 | 검증 실패 (@Valid, 잘못된 enum) |
| 404 | 리소스 미존재 |
| 409 | 낙관적 잠금 충돌 |
| 500 | 서버 오류 |

### 패키지
```
com.springjpatest.itemservice
├── ItemServiceApplication.java
├── entity/Item.java, ItemConst.java
├── dto/ItemListRequest.java, ItemListResponse.java, ItemRequest.java, ItemUpdateRequest.java, ItemResponse.java
├── repository/ItemRepository.java
├── service/ItemService.java
├── controller/ItemController.java
├── exception/GlobalExceptionHandler.java
└── config/CacheConfig.java
```

---

## 5. WebSocket Service (8085)

### 역할
- STOMP 기반 실시간 채팅
- 채팅방 생성/관리, 메시지 히스토리 (최근 50개)

### STOMP 설정

| 구분 | 값 |
|------|-----|
| 엔드포인트 | `/ws` (SockJS 폴백 지원) |
| 메시지 브로커 접두사 | `/topic` (인메모리) |
| 앱 목적지 접두사 | `/app` |

### 메시지 채널

| 구독/발송 | 목적지 | 설명 |
|-----------|--------|------|
| SUBSCRIBE | `/topic/chat.{roomId}` | 채팅방 구독 |
| SEND | `/app/chat.enter` | 입장 |
| SEND | `/app/chat.send` | 메시지 발송 |
| SEND | `/app/chat.leave` | 퇴장 |

### 메시지 DTO (`ChatMessage`)
- type: MessageType enum (ENTER, TALK, LEAVE)
- roomId, sender (닉네임), content, sentAt (서버 설정)

### REST API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| GET | `/api/chat/rooms` | 채팅방 목록 |
| POST | `/api/chat/rooms` | 채팅방 생성 |
| GET | `/api/chat/rooms/{roomId}` | 채팅방 조회 |
| GET | `/api/chat/rooms/{roomId}/messages` | 메시지 히스토리 |

### 동시성 처리
- 채팅방 저장: `LinkedHashMap` (순서 보존)
- 메시지 히스토리: `CopyOnWriteArrayList` (스레드 안전)
- 접속자 수: `ConcurrentHashMap`

### 패키지
```
com.springjpatest.websocketservice
├── WebsocketServiceApplication.java
├── dto/ChatMessage.java, ChatRoom.java
├── service/ChatService.java
├── controller/ChatRoomController.java, ChatController.java
└── config/WebSocketConfig.java
```

---

## 6. API Service (8083)

현재 미구현 상태 — Gateway에 라우팅 설정(`/api/external/**`)만 존재.

---

## 7. Frontend (3000)

### 기술
React 19, Vite 6, React Router v7, React Query v5, Axios

### 디렉토리 구조
```
frontend/src/
├── api/
│   ├── axiosClient.js      # axios 인스턴스 + 토큰 인터셉터
│   ├── authApi.js          # 회원가입, 로그인, 토큰 재발급
│   └── postApi.js          # 게시글 CRUD
├── hooks/
│   └── useAuth.js          # 인증 상태 관리 훅
├── pages/
│   ├── HomePage.jsx        # 게시글 목록
│   ├── PostDetailPage.jsx  # 게시글 상세
│   ├── LoginPage.jsx       # 로그인
│   └── SignupPage.jsx      # 회원가입
├── components/
│   └── PostCard.jsx        # 게시글 카드 컴포넌트
├── App.jsx                 # 라우팅
└── main.jsx                # React Query, BrowserRouter 초기화
```

### 라우팅

| 경로 | 페이지 |
|------|--------|
| `/` | HomePage |
| `/posts/:id` | PostDetailPage |
| `/login` | LoginPage |
| `/signup` | SignupPage |

### Axios 인터셉터 (`axiosClient.js`)
- 요청: localStorage 액세스 토큰 → `Authorization: Bearer {token}` 헤더 자동 첨부
- 응답: 401 수신 시 리프레시 토큰으로 재발급 후 원본 요청 재시도

### React Query 설정
- staleTime: 5분
- retry: 1회

### Vite 프록시 (`vite.config.js`)
- `/api/*` → `http://localhost:8080/api/*`
- 절대 경로: `@/` = `/src/`

---

## 보안 설계

### JWT 검증 계층화

```
Gateway (1차)        → 서명 + 만료 검증, 화이트리스트 처리
각 서비스 (2차)      → X-User-Email 헤더 신뢰 (Gateway 검증 전제)
```

### 토큰 저장
- 클라이언트: `localStorage` (XSS 취약성 주의)

### 비밀번호
- BCrypt (strength 10)

---

## 데이터베이스 프로파일

| 프로파일 | DB | ddl-auto | 비고 |
|----------|-----|----------|------|
| dev | H2 인메모리 | create-drop | `/h2-console` 활성화 |
| prod | PostgreSQL 16 | validate | 환경변수 주입 |

### 환경변수 (prod)

| 변수 | 설명 |
|------|------|
| `JWT_SECRET` | JWT 서명 키 (256비트 이상, 전 서비스 공통) |
| `DATABASE_URL` | PostgreSQL JDBC URL |
| `DB_USERNAME` | DB 사용자명 |
| `DB_PASSWORD` | DB 비밀번호 |

---

## Docker Compose 구성

| 컨테이너 | 이미지 | 포트 |
|---------|--------|------|
| postgres | postgres:16 | 5432 |
| auth-service | 빌드 | 8082 |
| blog-service | 빌드 | 8081 |
| gateway-service | 빌드 | 8080 |
| frontend | Nginx | 3000 |

DB 정보: `playgrounddb` / `playground` / `playground123` (로컬 개발용)
네트워크: `playground-net` (bridge)

---

## 로컬 개발 실행

### 백엔드 (개별 실행)
```bash
./gradlew :gateway-service:bootRun
./gradlew :blog-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :item-service:bootRun
./gradlew :websocket-service:bootRun
```

### 프론트엔드
```bash
cd frontend
npm install
npm run dev
```

### Docker Compose (전체)
```bash
docker-compose up -d
```

---

## 배포 (Render.com)

각 서비스를 별도 Web Service로 배포:

| 서비스 | 빌드 명령 | 시작 명령 |
|--------|-----------|-----------|
| gateway-service | `./gradlew :gateway-service:bootJar` | `java -jar gateway-service/build/libs/*.jar` |
| blog-service | `./gradlew :blog-service:bootJar` | `java -jar blog-service/build/libs/*.jar` |
| auth-service | `./gradlew :auth-service:bootJar` | `java -jar auth-service/build/libs/*.jar` |
| item-service | `./gradlew :item-service:bootJar` | `java -jar item-service/build/libs/*.jar` |
| frontend | `npm ci && npm run build` | Static Site |

환경변수 `SPRING_PROFILES_ACTIVE=prod` 설정 필수.

---

## 코딩 규칙

- **주석**: 한글로 상세히 작성
- **Java**: Google Java Style Guide
- **React**: Functional Component (화살표 함수)
- **패키지**: `com.springjpatest.{서비스명}`
