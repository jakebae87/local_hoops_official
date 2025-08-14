import axios from "axios";

const apiClient = axios.create({
  baseURL: "https://localbasket.p-e.kr/api",
  withCredentials: true,
});

export default apiClient;
