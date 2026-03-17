import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import App from './App.jsx'

/**
 * React 앱 초기화 진입점.
 *
 * - BrowserRouter: react-router-dom v7 라우팅 (브라우저 히스토리 기반)
 * - QueryClientProvider: React Query 전역 상태 관리 (서버 상태 캐싱)
 */

// React Query 클라이언트 설정
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 데이터 신선도 유지 시간: 5분
      staleTime: 1000 * 60 * 5,
      // 에러 발생 시 자동 재시도 횟수
      retry: 1,
    },
  },
})

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
