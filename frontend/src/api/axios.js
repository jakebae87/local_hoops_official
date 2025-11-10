import axios from "axios";

// API 전용 axios 인스턴스
const apiClient = axios.create({
  // 배포에서 /api 프록시 사용 (Nginx 등). 별도 .env 없어도 고정해도 됨.
  baseURL: process.env.VUE_APP_API_BASE_URL,
  withCredentials: false,
  timeout: 15000,
});

// ✅ 요청 인터셉터: 토큰이 있으면 항상 Authorization 부착
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

// ✅ 응답 인터셉터: 401이면 로그인 페이지로
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401) {
      localStorage.removeItem("token");
      // 로그인으로 이동 (필요 시 next 파라미터 유지)
      const here = window.location.pathname + window.location.search;
      window.location.href = `/login?next=${encodeURIComponent(here)}`;
    }
    return Promise.reject(error);
  }
);

export default apiClient;
