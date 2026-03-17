# ===== 1단계: Node.js로 Vite 빌드 =====
FROM node:22-alpine AS builder
WORKDIR /app

# package.json과 lock 파일만 먼저 복사 (npm install 캐시 활용)
COPY package.json package-lock.json ./
RUN npm ci

# 소스 코드 복사 후 프로덕션 빌드
COPY . .
RUN npm run build

# ===== 2단계: nginx로 정적 파일 서빙 =====
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html

# SPA 라우팅 지원: 모든 경로를 index.html로 폴백
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
