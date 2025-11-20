<template>
  <div style="max-width:360px;margin:48px auto">
    <h2>로그인</h2>
    <form @submit.prevent="onSubmit">
      <div style="margin:8px 0">
        <input v-model="email" placeholder="이메일" />
      </div>
      <div style="margin:8px 0">
        <input v-model="password" type="password" placeholder="비밀번호" />
      </div>
      <button type="submit">로그인</button>
    </form>
    <p v-if="error" style="color:red;margin-top:8px">{{ error }}</p>

    <div style="margin-top:16px">
      <router-link to="/signup">회원가입</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { login } from "@/api/auth";

const route = useRoute();
const router = useRouter();

const email = ref("");
const password = ref("");
const error = ref("");

const onSubmit = async () => {
  error.value = "";

  try {
    const res = await login({
      username: email.value,   // 백엔드 LoginRequest.username에 들어감
      password: password.value,
    });

    const token = res.data.token;
    localStorage.setItem("token", token);

    const next = route.query.next || "/";
    router.push(next);
  } catch (e) {
    error.value =
      e.response?.data?.message || "로그인에 실패했습니다. 다시 시도해 주세요.";
  }
};
</script>
