<template>
  <div class="admin-container">
    <h1 class="admin-title">📌 관리자 페이지</h1>
    <button class="refresh-btn" @click="fetchMarkerRequests">🔄 등록 요청 목록 불러오기</button>

    <div class="grid-wrapper">
      <!-- ✅ 테이블 헤더 -->
      <div class="grid-header">
        <div>ID</div>
        <div>제목</div>
        <div>위도</div>
        <div>경도</div>
        <div>등록일</div>
        <div>승인</div>
        <div>거부</div>
      </div>

      <!-- ✅ 데이터 리스트 (페이지네이션 적용) -->
      <div v-for="request in paginatedData" :key="request.id" class="grid-row">
        <div>{{ request.id }}</div>
        <div @click="openImageModal(request)" class="clickable-title">
          {{ request.title }}
        </div>
        <div>{{ request.latitude }}</div>
        <div>{{ request.longitude }}</div>
        <div>{{ new Date(request.created_at).toLocaleDateString() }}</div>
        <div><button class="approve-btn" @click="approveMarker(request.id)">✅ 승인</button></div>
        <div><button class="reject-btn" @click="rejectMarker(request.id)">❌ 거부</button></div>
      </div>
    </div>

    <!-- ✅ 페이지네이션 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">◀ 이전</button>
      <span>페이지 {{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages">다음 ▶</button>
    </div>

    <!-- ✅ 이미지 확대 모달 -->
    <div v-if="imageModalImages.length" class="image-modal">
      <button class="close-button" @click="closeImageModal">✖</button>
      <button v-if="imageModalImages.length > 1" class="nav-button left" @click="prevImage">◀</button>
      <img :src="getImagePath(imageModalImages[modalImageIndex])" class="modal-content" />
      <button v-if="imageModalImages.length > 1" class="nav-button right" @click="nextImage">▶</button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import apiClient from "@/api/axios";

export default {
  setup() {
    const markerRequests = ref([]);
    const imageModalImages = ref([]);
    const modalImageIndex = ref(0);

    // ✅ 페이지네이션 관련 변수
    const currentPage = ref(1);
    const itemsPerPage = 10;

    // ✅ 현재 페이지 데이터 계산
    const paginatedData = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage;
      const end = start + itemsPerPage;
      return markerRequests.value.slice(start, end);
    });

    // ✅ 총 페이지 수 계산
    const totalPages = computed(() => Math.ceil(markerRequests.value.length / itemsPerPage));

    // ✅ 페이지 이동 함수
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++;
      }
    };

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--;
      }
    };

    // ✅ 등록 요청 목록 불러오기
    const fetchMarkerRequests = async () => {
      try {
        const response = await apiClient.get("/api/markers/requests");
        markerRequests.value = response.data;
        currentPage.value = 1; // 새로 로드할 때 첫 페이지로 초기화
      } catch (error) {
        console.error("🚨 등록 요청 목록 불러오기 실패:", error);
      }
    };

    // ✅ 제목 클릭 시 해당 마커의 이미지 모달 열기
    const openImageModal = (marker) => {
      console.log("marker check: ", marker);
      if (marker.image) {
        const imageArray = typeof marker.image === "string" ? marker.image.split(",") : [];

        if (imageArray.length > 0) {
          imageModalImages.value = imageArray;
          modalImageIndex.value = 0;
        } else {
          alert("이미지가 없습니다.");
        }
      }
    };

    // ✅ 마커 승인
    const approveMarker = async (id) => {
      try {
        await apiClient.post(`/api/markers/approve/${id}`);
        alert("마커가 승인되었습니다.");
        fetchMarkerRequests();
      } catch (error) {
        console.error("🚨 마커 승인 실패:", error);
      }
    };

    // ✅ 마커 거부
    const rejectMarker = async (id) => {
      try {
        await apiClient.delete(`/api/markers/reject/${id}`);
        alert("마커가 거부되었습니다.");
        fetchMarkerRequests();
      } catch (error) {
        console.error("🚨 마커 거부 실패:", error);
      }
    };

    // ✅ 이미지 경로 변환
    const getImagePath = (img) => {
      console.log("img check:", img);
      if (!img) return "/default-image.png";
      return img.startsWith("/uploads/") ? `${process.env.VUE_APP_ASSET_BASE_URL}${img}` : img;
    };

    // ✅ 이미지 모달 닫기
    const closeImageModal = () => {
      imageModalImages.value = [];
    };

    // ✅ 이전 이미지 보기
    const prevImage = () => {
      modalImageIndex.value =
        modalImageIndex.value === 0 ? imageModalImages.value.length - 1 : modalImageIndex.value - 1;
    };

    // ✅ 다음 이미지 보기
    const nextImage = () => {
      modalImageIndex.value =
        modalImageIndex.value === imageModalImages.value.length - 1 ? 0 : modalImageIndex.value + 1;
    };

    onMounted(() => {
      fetchMarkerRequests();
    });

    return {
      markerRequests, paginatedData, totalPages, currentPage,
      fetchMarkerRequests, approveMarker, rejectMarker, openImageModal,
      imageModalImages, modalImageIndex, closeImageModal, prevImage, nextImage,
      getImagePath, nextPage, prevPage
    };
  },
};
</script>

<style>
/* ✅ 관리자 페이지 스타일 */
.admin-container {
  width: 90%;
  margin: 20px auto;
  text-align: center;
}

.admin-title {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 10px;
}

/* ✅ 새로고침 버튼 */
.refresh-btn {
  padding: 10px;
  background: #007bff;
  color: white;
  border: none;
  cursor: pointer;
  border-radius: 5px;
  font-size: 16px;
  margin-bottom: 15px;
}

.refresh-btn:hover {
  background: #0056b3;
}

/* ✅ 테이블 스타일 */
.grid-wrapper {
  display: flex;
  flex-direction: column;
  border: 1px solid #ddd;
  background: #f9f9f9;
  border-radius: 5px;
  overflow: hidden;
}

.grid-header, .grid-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  padding: 8px;
  border-bottom: 1px solid #ddd;
  align-items: center;
}

.clickable-title {
  cursor: pointer;
  color: #007bff;
  text-decoration: underline;
}

.clickable-title:hover {
  color: #0056b3;
}

/* ✅ 페이지네이션 */
.pagination {
  margin-top: 10px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.pagination button {
  padding: 8px 12px;
  border: none;
  cursor: pointer;
  background: #007bff;
  color: white;
  border-radius: 5px;
}

.pagination button:disabled {
  background: #ccc;
  cursor: not-allowed;
}
/* ✅ 이미지 확대 모달 (항상 브라우저 정중앙) */
.image-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 500px; /* 고정된 크기 */
  height: 500px;
  border-radius: 10px;
  z-index: 10001;
  overflow: hidden; /* 이미지가 넘치지 않도록 제한 */
}

/* ✅ 이미지 크기에 맞게 정렬 */
.modal-content {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 10px;
}

/* ✅ 닫기 버튼 */
.close-button {
  position: absolute;
  top: 10px;
  right: 20px;
  font-size: 20px;
  cursor: pointer;
  color: white;
  background: none;
  border: none;
}

/* ✅ 좌우 화살표 버튼 (팝업 내부에 고정) */
.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  font-size: 24px;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: white;
  cursor: pointer;
  padding: 10px;
  border-radius: 5px;
  transition: background 0.3s ease-in-out;
}

/* ✅ 좌우 버튼이 이미지 크기에 따라 조정되도록 */
.image-modal:hover .nav-button {
  display: block;
}

.nav-button:hover {
  background: rgba(0, 0, 0, 0.8);
}

/* ✅ 좌우 위치 고정 */
.left {
  left: 20px;
}

.right {
  right: 20px;
}

</style>
