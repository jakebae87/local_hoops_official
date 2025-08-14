<template>
  <div>
    <h1>우리 동네 농구코트</h1>
    <keep-alive>
      <MapView />
    </keep-alive>
  </div>
</template>

<script>
import MapView from "@/components/MapView.vue";

export default {
  name: "HomeView", // ✅ 컴포넌트 이름 변경
  components: { MapView },
};
</script>

<style scoped>
h1 {
  color: #333;
}
</style>
