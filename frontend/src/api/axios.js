import axios from "axios";

// 기본 axios 인스턴스 생성
const apiClient = axios.create({
  // 프로덕션(.env.production)에 VUE_APP_API_BASE_URL=/api 로 설정되어 있음
  baseURL: process.env.VUE_APP_API_BASE_URL || "/",
  withCredentials: false, // 쿠키 인증 안 쓰면 false가 안전
  timeout: 15000,
});

// ✅ 요청 인터셉터: JWT 토큰이 "있으면 무조건" 부착
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers = config.headers || {};
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ✅ 응답 인터셉터: 401 발생 시 로그인 페이지로 이동
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem("token");
      const here = window.location.pathname + window.location.search;
      window.location.href = `/login?next=${encodeURIComponent(here)}`;
    }
    return Promise.reject(error);
  }
);

export default apiClient;
