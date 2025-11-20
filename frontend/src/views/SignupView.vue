<template>
  <div style="max-width:360px;margin:48px auto">
    <h2>회원가입</h2>
    <form @submit.prevent="onSubmit">
      <div style="margin:8px 0">
        <input v-model="email" placeholder="이메일" />
      </div>
      <div style="margin:8px 0">
        <input v-model="nickname" placeholder="닉네임" />
      </div>
      <div style="margin:8px 0">
        <input v-model="password" type="password" placeholder="비밀번호" />
      </div>
      <button type="submit">회원가입</button>
    </form>
    <p v-if="message" style="color:green;margin-top:8px">{{ message }}</p>
    <p v-if="error" style="color:red;margin-top:8px">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { register } from "@/api/auth";

const email = ref("");
const nickname = ref("");
const password = ref("");

const message = ref("");
const error = ref("");

const router = useRouter();

const onSubmit = async () => {
  message.value = "";
  error.value = "";

  try {
    await register({
      email: email.value,
      nickname: nickname.value,
      password: password.value,
    });

    message.value = "회원가입이 완료되었습니다. 로그인 해 주세요.";
    setTimeout(() => router.push("/login"), 1500);
  } catch (e) {
    error.value = e.response?.data?.message || "회원가입에 실패했습니다.";
  }
};
</script>
