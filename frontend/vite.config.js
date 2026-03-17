import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Vite 개발 서버 설정.
 *
 * 개발 환경에서 /api/* 요청을 게이트웨이(8080)로 프록시하여
 * CORS 문제 없이 백엔드 API를 호출할 수 있다.
 */
export default defineConfig({
  plugins: [react()],

  server: {
    // 프론트엔드 개발 서버 포트
    port: 3000,

    proxy: {
      // /api로 시작하는 모든 요청을 게이트웨이 서버로 포워딩
      // 예: /api/posts → http://localhost:8080/api/posts
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  },

  // 절대 경로 임포트 설정 (예: import Foo from '@/components/Foo')
  resolve: {
    alias: {
      '@': '/src',
    }
  }
})
