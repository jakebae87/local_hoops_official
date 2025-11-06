
## 🎯 주요 변경 포인트

### 🟢 도메인 교체
모든 하드코딩된 주소를 새 도메인으로 변경했습니다.  
```
https://localbasket.p-e.kr  →  https://localbasket.o-r.kr
```
**변경된 파일**
- `frontend/src/api/axios.js`
- `frontend/src/components/MarkerPopup.vue`
- `frontend/src/views/AdminView.vue`
- `backend/src/main/java/com/example/demo/config/WebConfig.java`
- `docker-compose.yml` (`VIRTUAL_HOST`, `LETSENCRYPT_HOST`)

---

### ⚙️ 서버/빌드 환경 개선
- `backend/Dockerfile` → `openjdk:8-jdk` → `eclipse-temurin:8-jdk` 로 교체  
  (빌드 실패 방지 및 최신 LTS 호환성 확보)
- `gradlew` 실행 권한 부여 및 개행 문제 수정
- `docker-compose.yml` → `version:` 키 제거 (Compose v2 경고 해결)

---

### 🗄 DB 설정 변경
**파일:** `backend/src/main/resources/application.properties`  
내부 DB 주소 변경  
```
jdbc:mysql://192.168.x.x → jdbc:mysql://10.0.x.x
```
> 내부망 DB 이전에 따른 수정

---

### 🔒 보안 및 저장소 정리
- `data/certs/` 및 `*.pem`, `*.crt`, `*.key` 파일 **Git 히스토리 완전 제거**
- `.gitignore` 보강  
  ```
  data/certs/
  backend/uploads/
  *.pem
  *.crt
  *.key
  ```

---

### 🧱 배포/운영 절차 통합
1. 백엔드 빌드: `./gradlew clean bootJar`  
2. 도커 재빌드:  
   ```bash
   docker builder prune -f
   docker compose build --no-cache
   docker compose up -d
   ```

---

## ✅ 결과
- 새 도메인(`localbasket.o-r.kr`)으로 정상 서비스 동작
- Docker 및 Gradle 빌드 오류 해결
- 보안 파일 완전 정리, 안전한 저장소 상태 확보
- 내부망 DB 연결 반영 완료

---
