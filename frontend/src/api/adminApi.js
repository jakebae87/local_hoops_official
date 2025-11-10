// frontend/src/api/adminApi.js
import axios from "axios";

const adminApi = axios.create({
  baseURL: "/api",
  withCredentials: false,
  timeout: 15000,
});

adminApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (!token) {
      return Promise.reject(new Error("관리자 인증 토큰이 없습니다. 다시 로그인하세요."));
    }
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
    return config;
  },
  (error) => Promise.reject(error)
);

adminApi.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      console.error("관리자 권한이 필요합니다. 다시 로그인하세요.");
    }
    return Promise.reject(error);
  }
);

export default adminApi;
