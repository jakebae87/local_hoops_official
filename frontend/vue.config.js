module.exports = {
  devServer: {
    port: 8080,
    proxy: {
      "/api": {
        target: process.env.VUE_APP_API_BASE_URL || "http://localhost:9000",
        changeOrigin: true,
      },
    },
  },
};
