<template>
  <div class="popup-overlay">
    <div class="popup">
      <h3 class="popup-title">{{ title }}</h3>
      <input
        v-if="!isDetail"
        v-model="title"
        placeholder="설명을 입력해 주세요."
        class="popup-input"
        maxlength="30"
      />

      <div v-if="images.length" class="popup-images">
        <img
          v-for="(img, index) in images"
          :key="index"
          :src="getImagePath(img)"
          class="popup-img"
          @click="openImageModal(index)"
        />
      </div>

      <div class="file-upload-section" v-if="!isDetail">
        <p v-if="images.length === 0" class="file-upload-info">
          이미지 파일은 최대 3개
        </p>
        <input type="file" multiple @change="onFileChange" class="popup-file" />
      </div>

      <!-- ✅ 댓글 목록 및 입력 (상세보기일 때만) -->
      <div v-if="isDetail" class="comment-section">
        <div
          class="comment-list"
          ref="commentList"
          @scroll.passive="handleScroll"
        >
          <div
            v-for="comment in comments"
            :key="comment.comment_id"
            class="comment-item"
          >
            <p>{{ comment.content }}</p>
            <small>{{ formatDate(comment.created_at) }}</small>
          </div>
          <div v-if="loadingComments" class="loading">불러오는 중...</div>
        </div>

        <div class="comment-input-wrapper">
          <textarea
            v-model="commentInput"
            placeholder="다른 사람에게 불쾌감을 주는 욕설, 혐오, 비하의 표현은 주의해주세요."
            class="popup-comment"
            maxlength="40"
          ></textarea>
          <div class="comment-controls">
            <span>{{ commentInput.length }}/40</span>
            <button
              class="btn btn-save"
              @click="submitComment"
              :disabled="!commentInput.trim()"
            >
              등록
            </button>
          </div>
        </div>
      </div>

      <div class="popup-buttons">
        <button @click="closePopup" class="btn">닫기</button>
        <button v-if="!isDetail" @click="saveMarker" class="btn btn-save">
          저장
        </button>
      </div>
    </div>

    <div v-if="modalImageIndex !== null" class="image-modal">
      <img :src="getImagePath(images[modalImageIndex])" class="modal-content" />
      <button class="close-button" @click="closeImageModal">✖</button>
    </div>
  </div>
</template>

<script>
import { ref, watch } from "vue";
import apiClient from "@/api/axios";

export default {
  props: ["marker", "isDetail", "position"],
  emits: ["close", "save"],
  setup(props, { emit }) {
    const title = ref("");
    const images = ref([]);
    const comment = ref("");
    const commentInput = ref("");
    const comments = ref([]);
    const page = ref(1);
    const pageSize = 5;
    const loadingComments = ref(false);
    const endOfComments = ref(false);
    const commentList = ref(null);

    const modalImageIndex = ref(null);
    const loading = ref(false);

    const getImagePath = (img) => {
      if (!img) return "/default-image.png";
      return typeof img === "string"
        ? img.startsWith("/uploads/")
          ? `${process.env.VUE_APP_ASSET_BASE_URL}${img}`
          : img
        : img instanceof File
        ? URL.createObjectURL(img)
        : "/default-image.png";
    };

    const fetchComments = async () => {
      if (loadingComments.value || endOfComments.value || !props.marker?.id)
        return;
      loadingComments.value = true;
      try {
        const res = await apiClient.get(`/comments/${props.marker.id}`, {
          params: { page: page.value, size: pageSize },
        });
        if (res.data.length < pageSize) endOfComments.value = true;
        comments.value.push(...res.data);
        page.value++;
      } catch (err) {
        console.error("댓글 불러오기 실패:", err);
      } finally {
        loadingComments.value = false;
      }
    };

    const submitComment = async () => {
      const confirmSubmit = window.confirm("이 댓글을 등록하시겠습니까?");
      if (!confirmSubmit) return;

      try {
        await apiClient.post("/comments", {
          markerId: props.marker.id,
          content: commentInput.value,
        });
        commentInput.value = "";
        page.value = 1;
        comments.value = [];
        endOfComments.value = false;
        fetchComments();
      } catch (err) {
        alert("댓글 등록 중 오류 발생");
      }
    };

    const handleScroll = () => {
      const list = commentList.value;
      if (!list) return;
      const nearBottom =
        list.scrollTop + list.clientHeight >= list.scrollHeight - 10;
      if (nearBottom) fetchComments();
    };

    watch(
      () => props.marker,
      (newMarker) => {
        if (newMarker) {
          title.value = newMarker.title || "";
          images.value = Array.isArray(newMarker.images)
            ? newMarker.images
            : typeof newMarker.images === "string"
            ? newMarker.images.split(",")
            : [];

          if (props.isDetail) {
            page.value = 1;
            comments.value = [];
            endOfComments.value = false;
            fetchComments();
          }
        }
      },
      { immediate: true }
    );

    const onFileChange = (event) => {
      if (props.isDetail) return;
      const files = event.target.files;
      if (files.length > 3) {
        alert("최대 3개의 이미지만 업로드할 수 있습니다.");
        return;
      }
      images.value = Array.from(files).filter((file) => {
        if (!file.type.startsWith("image/")) {
          alert("이미지 파일만 업로드 가능합니다.");
          return false;
        }
        return true;
      });
    };

    const saveMarker = async () => {
      const { latitude, longitude } = props.position;
      if (!title.value || images.value.length === 0) {
        alert("제목과 최소 1장의 이미지를 등록해야 합니다.");
        return;
      }

      loading.value = true;

      try {
        // ✅ AI 서버 호출 제거 (기존 루프 삭제)

        const formData = new FormData();
        formData.append("title", title.value);
        formData.append("latitude", latitude.toString());  // 🔥 추가
        formData.append("longitude", longitude.toString());  // 🔥 추가
        images.value.forEach((image) => formData.append("images", image)); // ← 중요: 파라미터 이름은 백엔드와 일치해야 함

        const response = await apiClient.post(
          "/markers/request",
          formData,
          {
            headers: { "Content-Type": "multipart/form-data" },
          }
        );

        if (response.data.error) {
          alert(response.data.error);
        } else {
          alert(response.data.message);
          emit("save");
          closePopup();
        }
      } catch (error) {
        console.error("🚨 마커 저장 요청 실패:", error);
        alert("🚨 오류 발생: " + (error.response?.data?.message || "네트워크 오류"));
      } finally {
        loading.value = false;
      }
    };

    const openImageModal = (index) => (modalImageIndex.value = index);
    const closeImageModal = () => (modalImageIndex.value = null);
    const closePopup = () => emit("close");
    const formatDate = (dateStr) =>
      new Date(dateStr).toLocaleDateString("ko-KR");

    return {
      title,
      images,
      comment,
      commentInput,
      comments,
      commentList,
      onFileChange,
      saveMarker,
      closePopup,
      getImagePath,
      modalImageIndex,
      openImageModal,
      closeImageModal,
      submitComment,
      handleScroll,
      loadingComments,
      formatDate,
    };
  },
};
</script>

<style>
.popup-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}
.popup {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  width: 50%;
  max-width: 420px;
  max-height: 90vh;
  text-align: left;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}
.popup-input {
  font-size: 16px;
  padding: 5px;
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 5px;
}
.popup-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 15px;
}
.popup-images {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
}
.popup-img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 5px;
  cursor: pointer;
}
.popup-comment {
  width: 100%;
  height: 50px;
  border: 1px solid #ddd;
  box-sizing: border-box;
  border-radius: 5px;
  resize: none;
  font-size: 14px;
}
.comment-input-wrapper {
  margin-top: 10px;
}
.comment-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
}
.btn-comment {
  width: 80px;
  padding: 6px;
  margin-left: auto;
}
.comment-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.comment-list {
  max-height: 120px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 5px;
  padding: 5px;
}
.comment-item {
  border-bottom: 1px solid #eee;
  padding: 5px 0;
}
.comment-item:last-child {
  border-bottom: none;
}
.loading {
  text-align: center;
  color: #999;
  font-size: 14px;
}
.popup-buttons {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 10px;
}
.btn {
  flex: 1;
  padding: 8px;
  margin: 0 5px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
.btn-save {
  background: #4caf50;
  color: white;
}
.image-modal {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  width: 500px;
  height: 500px;
  border-radius: 10px;
  z-index: 10001;
}
.modal-content {
  max-width: 100%;
  max-height: 100%;
  border-radius: 10px;
}
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
</style>
