import axios from "axios";

// 기본 axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || "/", // 기본 경로 보완
  withCredentials: true, // 필요시 쿠키도 포함 가능
});

// ✅ 요청 인터셉터: JWT 토큰 자동 첨부
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token && config.url?.startsWith("/api")) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ✅ 응답 인터셉터 (선택): 401 발생 시 로그인 페이지로 이동
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default apiClient;
