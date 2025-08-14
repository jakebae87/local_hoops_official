import { createRouter, createWebHistory } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import AdminView from "@/views/AdminView.vue";

const routes = [
  {
    path: "/",
    name: "Home",
    component: HomeView,
    meta: { isAdmin: false }, // 일반 사용자 모드
  },
  {
    path: "/admin",
    name: "Admin",
    component: AdminView,
    meta: { isAdmin: true }, // 관리자 모드
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// ✅ 페이지 이동할 때 관리자 여부 판단
router.beforeEach((to, from, next) => {
  if (to.meta.isAdmin) {
    localStorage.setItem("isAdmin", "true"); // ✅ 관리자 모드 설정
  } else {
    localStorage.removeItem("isAdmin"); // ✅ 일반 사용자 모드 설정
  }
  next();
});

export default router;
