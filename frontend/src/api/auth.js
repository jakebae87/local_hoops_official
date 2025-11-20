import apiClient from "./axios";

export const register = (payload) =>
  apiClient.post("/auth/register", payload);

export const login = (payload) =>
  apiClient.post("/auth/login", payload);
