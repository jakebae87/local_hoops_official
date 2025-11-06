import axios from "axios";

const apiClient = axios.create({
  baseURL: "https://localbasket.o-r.kr/api",
  withCredentials: true,
});

export default apiClient;
