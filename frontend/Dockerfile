# 1단계: 빌드용
FROM node:18 AS build-stage
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# 2단계: 정적 웹 서버로 서빙
FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
