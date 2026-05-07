# PlayGround 프론트엔드

React 19 기반 기술 블로그 프론트엔드. Spring MSA 백엔드(gateway:8080)와 연동한다.

---

## 기술 스택

| 항목 | 버전 | 용도 |
|------|------|------|
| React | 19 | UI 라이브러리 |
| Vite | 6 | 번들러 / 개발 서버 |
| React Router | v7 | 클라이언트 라우팅 |
| React Query | v5 | 서버 상태 캐싱 |
| Axios | 1.7 | HTTP 클라이언트 |
| Tailwind CSS | v4 | 유틸리티 CSS (config 파일 불필요) |
| @stomp/stompjs | 7 | STOMP WebSocket 클라이언트 |
| sockjs-client | 1.6 | SockJS 트랜스포트 |
| react-markdown | 9 | 블로그 본문 마크다운 렌더링 |

---

## 디렉토리 구조

```
frontend/
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── index.css              # Tailwind 임포트 + 마크다운(.prose) 전역 스타일
    ├── main.jsx               # 앱 진입점 (QueryClient, BrowserRouter, AuthProvider)
    ├── App.jsx                # 라우트 정의
    ├── context/
    │   └── AuthContext.jsx    # 전역 인증 상태 (isLoggedIn, userEmail, saveTokens, clearTokens)
    ├── api/
    │   ├── axiosClient.js     # Axios 인스턴스 + 토큰 인터셉터
    │   ├── authApi.js         # 회원가입 / 로그인 / 토큰 재발급
    │   ├── postApi.js         # 게시글 CRUD
    │   ├── itemApi.js         # 아이템 CRUD + 목록 조회
    │   └── chatApi.js         # 채팅방 REST API + 메시지 히스토리
    ├── hooks/
    │   ├── useAuth.js         # 로그인/로그아웃/회원가입 액션 훅
    │   └── useWebSocket.js    # STOMP 연결 관리 훅
    ├── components/
    │   ├── Layout.jsx         # Navbar + Outlet 래퍼
    │   ├── Navbar.jsx         # 상단 내비게이션 (반응형, 인증 상태 반영)
    │   ├── PrivateRoute.jsx   # 미인증 시 /login 리다이렉트
    │   └── PostCard.jsx       # 게시글 카드
    └── pages/
        ├── HomePage.jsx       # 블로그 게시글 목록 + 페이지네이션
        ├── PostDetailPage.jsx # 게시글 상세 + 마크다운 렌더링 + 수정/삭제
        ├── WritePostPage.jsx  # 게시글 작성 / 수정 통합 페이지
        ├── LoginPage.jsx      # 로그인 폼
        ├── SignupPage.jsx     # 회원가입 폼
        ├── ItemsPage.jsx      # 아이템 목록 (필터 + 테이블 + 등록 모달)
        ├── ChatPage.jsx       # 채팅방 목록 + 방 생성
        └── ChatRoomPage.jsx   # 실시간 채팅 (STOMP)
```

---

## 라우트 구조

| 경로 | 페이지 | 인증 필요 |
|------|--------|-----------|
| `/` | HomePage | 불필요 |
| `/posts/:id` | PostDetailPage | 불필요 |
| `/posts/write` | WritePostPage | 필요 |
| `/posts/:id/edit` | WritePostPage | 필요 |
| `/items` | ItemsPage | 불필요 (등록만 필요) |
| `/chat` | ChatPage | 불필요 |
| `/chat/:roomId` | ChatRoomPage | 불필요 (닉네임 입력) |
| `/login` | LoginPage | - |
| `/signup` | SignupPage | - |

`/login`, `/signup` 은 Layout(Navbar) 없이 독립 렌더링.  
그 외 모든 경로는 `Layout` 컴포넌트로 감싸진다.

---

## 인증 흐름

```
AuthProvider (Context)
    └── isLoggedIn / userEmail / saveTokens / clearTokens

useAuth (Hook)
    └── login()  → loginApi() → saveTokens() → navigate('/')
    └── logout() → clearTokens() → navigate('/login')
    └── signup() → signupApi() → navigate('/login')

axiosClient (인터셉터)
    └── 요청: localStorage.accessToken → Authorization 헤더 자동 첨부
    └── 응답 401: refreshToken으로 재발급 → 원본 요청 재시도 → 실패 시 /login 이동
```

JWT 페이로드의 `sub` 필드를 클라이언트에서 디코딩해 `userEmail`로 노출한다(서명 검증 없음, UI 표시 전용).

---

## 백엔드 연동 (Vite 프록시)

개발 서버에서 CORS 없이 게이트웨이를 호출한다.

```js
// vite.config.js
proxy: {
  '/api': { target: 'http://localhost:8080', changeOrigin: true },
  '/ws':  { target: 'http://localhost:8080', changeOrigin: true, ws: true },
}
```

| 프록시 경로 | 게이트웨이 → 대상 서비스 |
|-------------|--------------------------|
| `/api/posts/**` | → blog-service:8081 |
| `/api/auth/**` | → auth-service:8082 |
| `/api/items/**` | → item-service:8084 |
| `/api/chat/**` | → websocket-service:8085 |
| `/ws/**` | → websocket-service:8085 (WebSocket 업그레이드) |

---

## WebSocket 채팅 구조

`useWebSocket` 훅이 STOMP 연결 생명주기를 관리한다.

```
컴포넌트 마운트
    └── Client.activate() → SockJS('/ws') 연결
        └── onConnect: SUBSCRIBE /topic/chat.{roomId}
                       PUBLISH /app/chat.enter
컴포넌트 언마운트
    └── PUBLISH /app/chat.leave
        client.deactivate()
```

**메시지 타입**

| type | 표시 방식 |
|------|-----------|
| ENTER | 중앙 시스템 메시지 ("xxx 님이 입장하셨습니다") |
| TALK | 말풍선 (내 메시지: 오른쪽 인디고, 상대: 왼쪽 흰색) |
| LEAVE | 중앙 시스템 메시지 ("xxx 님이 퇴장하셨습니다") |

비로그인 사용자는 채팅방 입장 시 닉네임을 직접 입력한다.

---

## 아이템 페이지 필터 파라미터

`item-service`의 `ItemListRequest`와 1:1 대응.

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| keyword | string | 상품명 LIKE 검색 |
| itemType | enum | ALL / GAME_MONEY / ITEM / ACCOUNT / ETC |
| server | string | 서버명 |
| minPrice | number | 최소 가격 |
| maxPrice | number | 최대 가격 |
| sort | enum | RegDateMax(기본) / RegDateMin / PriceMin / PriceMax |
| page | number | 페이지 번호 (0부터) |
| size | number | 페이지 크기 (기본 15) |

---

## 스타일 시스템

Tailwind CSS v4 (`@tailwindcss/vite` 플러그인 방식, `tailwind.config.js` 불필요).

```css
/* src/index.css */
@import "tailwindcss";

/* 마크다운 렌더링용 .prose 클래스 (h1~h3, code, pre, ul, table 등) */
```

주요 색상 팔레트:

| 용도 | 클래스 |
|------|--------|
| 주요 액션 (버튼, 링크) | `bg-indigo-600 hover:bg-indigo-700` |
| 배경 | `bg-gray-50` |
| 카드/패널 | `bg-white border border-gray-200` |
| 본문 텍스트 | `text-gray-900` |
| 보조 텍스트 | `text-gray-500`, `text-gray-400` |
| 에러 | `text-red-600 bg-red-50` |

---

## 로컬 실행

```bash
cd frontend
npm install
npm run dev     # http://localhost:3000
```

백엔드 서비스들이 먼저 실행되어 있어야 한다.

```bash
# 루트에서
./gradlew :gateway-service:bootRun
./gradlew :auth-service:bootRun
./gradlew :blog-service:bootRun
./gradlew :item-service:bootRun
./gradlew :websocket-service:bootRun
```

---

## 빌드 및 배포

```bash
npm run build   # dist/ 폴더 생성
```

Render.com 배포 시: Static Site 타입, 빌드 명령 `npm ci && npm run build`, 퍼블리시 디렉토리 `dist`.
