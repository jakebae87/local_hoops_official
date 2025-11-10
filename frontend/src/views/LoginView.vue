<template>
  <div style="max-width:360px;margin:48px auto">
    <h2>관리자 로그인</h2>
    <form @submit.prevent="onSubmit">
      <div style="margin:8px 0">
        <input v-model="username" placeholder="아이디" />
      </div>
      <div style="margin:8px 0">
        <input v-model="password" type="password" placeholder="비밀번호" />
      </div>
      <button type="submit">로그인</button>
    </form>
    <p v-if="error" style="color:red;margin-top:8px">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import apiClient from "@/api/axios";

const route = useRoute();
const router = useRouter();

const username = ref("");
const password = ref("");
const error = ref("");

const onSubmit = async () => {
  error.value = "";
  try {
    const { data } = await apiClient.post("/auth/login", {
      username: username.value,
      password: password.value,
    });
    localStorage.setItem("token", data.token);
    const next = route.query.next || "/admin";
    router.replace(next);
  } catch (e) {
    error.value = "아이디 또는 비밀번호가 올바르지 않습니다.";
  }
};
</script>
