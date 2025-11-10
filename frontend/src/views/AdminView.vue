<template>
  <div class="admin-container">
    <h1 class="admin-title">📌 관리자 페이지</h1>

    <button class="refresh-btn" @click="fetchMarkerRequests" :disabled="loading">
      🔄 등록 요청 목록 불러오기
    </button>

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
        <div>
          <button class="approve-btn" @click="approveMarker(request.id)" :disabled="actionBusy">
            ✅ 승인
          </button>
        </div>
        <div>
          <button class="reject-btn" @click="rejectMarker(request.id)" :disabled="actionBusy">
            ❌ 거부
          </button>
        </div>
      </div>

      <div v-if="!loading && paginatedData.length === 0" class="empty">
        대기 중인 요청이 없습니다.
      </div>
    </div>

    <!-- ✅ 페이지네이션 -->
    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 1">◀ 이전</button>
      <span>페이지 {{ currentPage }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage === totalPages || totalPages === 0">다음 ▶</button>
    </div>

    <!-- ✅ 이미지 확대 모달 -->
    <div v-if="imageModalImages.length" class="image-modal" @click.self="closeImageModal">
      <button class="close-button" @click="closeImageModal">✖</button>
      <button v-if="imageModalImages.length > 1" class="nav-button left" @click="prevImage">◀</button>
      <img :src="getImagePath(imageModalImages[modalImageIndex])" class="modal-content" />
      <button v-if="imageModalImages.length > 1" class="nav-button right" @click="nextImage">▶</button>
    </div>

    <div v-if="error" class="error">⚠️ {{ error }}</div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from "vue";
import adminApi from "@/api/adminApi"; // ✅ 관리자 전용 인스턴스 (항상 토큰 부착)

export default {
  name: "AdminView",
  setup() {
    const markerRequests = ref([]);
    const imageModalImages = ref([]);
    const modalImageIndex = ref(0);

    const loading = ref(false);
    const actionBusy = ref(false);
    const error = ref(null);

    // ✅ 페이지네이션
    const currentPage = ref(1);
    const itemsPerPage = 10;

    const totalPages = computed(() =>
      Math.ceil(markerRequests.value.length / itemsPerPage)
    );

    const paginatedData = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage;
      const end = start + itemsPerPage;
      return markerRequests.value.slice(start, end);
    });

    const nextPage = () => {
      if (currentPage.value < totalPages.value) currentPage.value++;
    };
    const prevPage = () => {
      if (currentPage.value > 1) currentPage.value--;
    };

    // ✅ 등록 요청 목록 불러오기 (ADMIN 전용)
    const fetchMarkerRequests = async () => {
      loading.value = true;
      error.value = null;
      try {
        const { data } = await adminApi.get("/markers/requests");
        markerRequests.value = Array.isArray(data) ? data : [];
        currentPage.value = 1;
      } catch (e) {
        console.error("🚨 등록 요청 목록 불러오기 실패:", e);
        error.value =
          e?.response?.data?.message ||
          e?.message ||
          "요청 목록을 불러오지 못했습니다.";
      } finally {
        loading.value = false;
      }
    };

    // ✅ 제목 클릭 → 이미지 모달
    const openImageModal = (marker) => {
      if (!marker) return;
      // 백엔드에서 image(단일) 또는 images(콤마 문자열) 둘 중 하나가 올 수 있으니 대응
      const raw =
        marker.images ??
        marker.image ??
        "";
      const list =
        typeof raw === "string" && raw.trim().length > 0
          ? raw.split(",").map((s) => s.trim()).filter(Boolean)
          : Array.isArray(raw)
          ? raw
          : [];

      if (list.length > 0) {
        imageModalImages.value = list;
        modalImageIndex.value = 0;
      } else {
        alert("이미지가 없습니다.");
      }
    };

    // ✅ 승인
    const approveMarker = async (id) => {
      if (!id) return;
      actionBusy.value = true;
      try {
        await adminApi.post(`/markers/approve/${id}`);
        alert("마커가 승인되었습니다.");
        await fetchMarkerRequests();
      } catch (e) {
        console.error("🚨 마커 승인 실패:", e);
        alert(e?.response?.data?.message || "승인에 실패했습니다.");
      } finally {
        actionBusy.value = false;
      }
    };

    // ✅ 거부
    const rejectMarker = async (id) => {
      if (!id) return;
      actionBusy.value = true;
      try {
        await adminApi.delete(`/markers/reject/${id}`);
        alert("마커가 거부되었습니다.");
        await fetchMarkerRequests();
      } catch (e) {
        console.error("🚨 마커 거부 실패:", e);
        alert(e?.response?.data?.message || "거부에 실패했습니다.");
      } finally {
        actionBusy.value = false;
      }
    };

    // ✅ 이미지 경로 변환
    const getImagePath = (img) => {
      if (!img) return "/default-image.png";
      // 업로드 경로라면 정적 자산 베이스 붙이기 (예: https://localbasket.o-r.kr)
      const base = import.meta.env.VITE_ASSET_BASE_URL || process.env.VUE_APP_ASSET_BASE_URL || "";
      if (img.startsWith("/uploads/")) return `${base}${img}`;
      return img;
    };

    // ✅ 모달 제어
    const closeImageModal = () => {
      imageModalImages.value = [];
    };
    const prevImage = () => {
      const n = imageModalImages.value.length;
      modalImageIndex.value = (modalImageIndex.value - 1 + n) % n;
    };
    const nextImage = () => {
      const n = imageModalImages.value.length;
      modalImageIndex.value = (modalImageIndex.value + 1) % n;
    };

    onMounted(() => {
      fetchMarkerRequests();
    });

    return {
      // data
      markerRequests,
      imageModalImages,
      modalImageIndex,
      loading,
      actionBusy,
      error,
      // pagination
      paginatedData,
      totalPages,
      currentPage,
      nextPage,
      prevPage,
      // actions
      fetchMarkerRequests,
      approveMarker,
      rejectMarker,
      openImageModal,
      // modal utils
      getImagePath,
      closeImageModal,
      prevImage,
      nextImage,
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
.refresh-btn[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}
.refresh-btn:hover:not([disabled]) {
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

.grid-row { background: #fff; }
.grid-row:nth-child(even) { background: #fafafa; }

.clickable-title {
  cursor: pointer;
  color: #007bff;
  text-decoration: underline;
}
.clickable-title:hover {
  color: #0056b3;
}

/* ✅ 빈 상태 */
.empty {
  padding: 16px;
  color: #666;
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
  width: 500px;
  height: 500px;
  border-radius: 10px;
  z-index: 10001;
  overflow: hidden;
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
.image-modal:hover .nav-button { display: block; }

.nav-button:hover { background: rgba(0, 0, 0, 0.8); }

/* ✅ 좌우 위치 고정 */
.left { left: 20px; }
.right { right: 20px; }
</style>
