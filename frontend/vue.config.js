// const { defineConfig } = require('@vue/cli-service')
// module.exports = defineConfig({
//   transpileDependencies: true
// })

module.exports = {
  devServer: {
    port: 8080, // Vue.js 개발 서버 포트
    proxy: {
      "/api": {
        target: "http://localhost:9000", // ✅ 백엔드 API 주소
        changeOrigin: true
      }
    }
  }
};
