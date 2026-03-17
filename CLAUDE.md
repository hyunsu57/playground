# 프로젝트 개요

Spring 기술 블로그 - Spring 공부 내용을 설명하고 기능을 직접 사용해볼 수 있는 기술 블로그.

## 프로젝트 구조

```
playGround/
├── gateway-service/    # API 게이트웨이 (포트 8080) - Spring Cloud Gateway
├── blog-service/       # 블로그 게시글 API (포트 8081) - Spring Web MVC + JPA
├── auth-service/       # 인증/인가 서비스 (포트 8082) - Spring Security + JWT
├── frontend/           # React 19 + Vite (포트 3000)
└── docker-compose.yml  # 로컬 전체 환경 오케스트레이션
```

## 기술 스택

- **백엔드**: Spring Boot 4.0.3 (Spring 6), Java 21, Spring Cloud 2025.1.0
- **프론트엔드**: React 19, Vite 6, React Router v7, React Query v5, Axios
- **데이터베이스**: H2 (개발), PostgreSQL (운영)
- **인증**: JWT (jjwt 0.12.x), BCrypt
- **배포**: Render.com (GitHub 연동)

## 코딩 규칙

- **주석**: 한글로 상세히 작성
- **Java 스타일**: Google Java Style Guide
- **React 스타일**: Functional Component (함수형 컴포넌트, 화살표 함수)
- **패키지**: `com.springjpatest.{서비스명}`

## 서비스별 API 라우팅

| 경로 | 서비스 | 포트 |
|------|--------|------|
| `/api/posts/**`, `/api/categories/**` | blog-service | 8081 |
| `/api/auth/**` | auth-service | 8082 |

## 로컬 개발 실행 방법

### 백엔드 (개별 실행)
```bash
# gateway-service
./gradlew :gateway-service:bootRun

# blog-service
./gradlew :blog-service:bootRun

# auth-service
./gradlew :auth-service:bootRun
```

### 프론트엔드
```bash
cd frontend
npm install
npm run dev
```

### Docker Compose (전체 환경)
```bash
docker-compose up -d
```

## 환경변수

| 변수 | 설명 | 기본값 |
|------|------|--------|
| `JWT_SECRET` | JWT 서명 키 (256비트 이상) | 개발용 기본값 설정됨 |
| `DATABASE_URL` | PostgreSQL URL (prod) | H2 인메모리 (dev) |
| `DB_USERNAME` | DB 사용자명 (prod) | - |
| `DB_PASSWORD` | DB 비밀번호 (prod) | - |

## Render.com 배포

각 서비스를 별도 Web Service로 배포:
- `./gradlew :gateway-service:bootJar` → `java -jar gateway-service/build/libs/*.jar`
- `./gradlew :blog-service:bootJar` → `java -jar blog-service/build/libs/*.jar`
- `./gradlew :auth-service:bootJar` → `java -jar auth-service/build/libs/*.jar`
- `frontend/` → Static Site (`npm ci && npm run build`)
