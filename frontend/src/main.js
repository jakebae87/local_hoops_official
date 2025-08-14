import { createApp } from "vue";
import App from "./App.vue";
import router from "./router"; // Vue Router 불러오기

const app = createApp(App);
app.use(router); // Vue에 라우터 적용
app.mount("#app");
