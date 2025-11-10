import { createRouter, createWebHistory } from "vue-router";
import HomeView from "@/views/HomeView.vue";
import AdminView from "@/views/AdminView.vue";
import LoginView from "@/views/LoginView.vue";

function decodeJwt(token) {
  try {
    const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join("")
    );
    return JSON.parse(json);
  } catch {
    return null;
  }
}

const routes = [
  { path: "/", name: "Home", component: HomeView },
  { path: "/login", name: "Login", component: LoginView },
  { path: "/admin", name: "Admin", component: AdminView, meta: { requiresAdmin: true } },
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  if (to.meta?.requiresAdmin) {
    const token = localStorage.getItem("token");
    if (!token) {
      return next({ path: "/login", query: { next: to.fullPath } });
    }
    const payload = decodeJwt(token) || {};
    const roles = payload.roles || payload.authorities || [];
    const hasAdmin =
      Array.isArray(roles) ? roles.includes("ROLE_ADMIN") : String(roles).includes("ROLE_ADMIN");

    if (!hasAdmin) {
      // 관리자 권한 없으면 로그인으로
      return next({ path: "/login", query: { next: to.fullPath } });
    }
  }
  next();
});

export default router;
