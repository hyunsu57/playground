import axios from 'axios'

/**
 * 공용 Axios 클라이언트 인스턴스.
 *
 * - baseURL: 개발 환경에서는 vite.config.js의 프록시를 통해 게이트웨이로 전달
 * - 요청 인터셉터: localStorage의 액세스 토큰을 Authorization 헤더에 자동 첨부
 * - 응답 인터셉터: 401 응답 시 리프레시 토큰으로 액세스 토큰 재발급 후 재시도
 */
const axiosClient = axios.create({
  baseURL: '/api',           // vite 프록시가 http://localhost:8080/api로 포워딩
  timeout: 10000,            // 요청 타임아웃: 10초
  headers: {
    'Content-Type': 'application/json',
  },
})

// ===== 요청 인터셉터 =====
// 모든 API 요청 전에 localStorage의 액세스 토큰을 헤더에 추가
axiosClient.interceptors.request.use(
  (config) => {
    const accessToken = localStorage.getItem('accessToken')
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ===== 응답 인터셉터 =====
// 401 응답 시 리프레시 토큰으로 액세스 토큰 재발급 후 원래 요청 재시도
axiosClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // 401 응답이고 재시도하지 않은 요청인 경우 (무한 루프 방지: _retry 플래그)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        if (!refreshToken) {
          // 리프레시 토큰 없음 → 로그인 페이지로 이동
          window.location.href = '/login'
          return Promise.reject(error)
        }

        // 리프레시 토큰으로 새 액세스 토큰 발급 요청
        const { data } = await axios.post('/api/auth/refresh', null, {
          headers: { Authorization: `Bearer ${refreshToken}` },
        })

        // 새 토큰을 localStorage에 저장
        localStorage.setItem('accessToken', data.accessToken)
        localStorage.setItem('refreshToken', data.refreshToken)

        // 원래 요청 재시도 (새 액세스 토큰으로)
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
        return axiosClient(originalRequest)

      } catch (refreshError) {
        // 리프레시 토큰도 만료 → 로그아웃 처리
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

export default axiosClient
