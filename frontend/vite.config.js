import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

/**
 * Vite 개발 서버 설정.
 *
 * /api/* → 게이트웨이(8080)로 프록시
 * /ws/*  → 게이트웨이(8080)를 통해 websocket-service(8085)로 라우팅
 */
export default defineConfig({
  plugins: [react(), tailwindcss()],

  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },

  resolve: {
    alias: {
      '@': '/src',
    },
  },
})
