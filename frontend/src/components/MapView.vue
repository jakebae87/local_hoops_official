<template>
  <div>
    <div id="map" ref="mapContainer" class="map-container"></div>

    <MarkerPopup
      v-if="showPopup"
      :position="currentPosition"
      :marker="selectedMarker"
      :isDetail="isDetail"
      @close="closePopup"
      @save="fetchMarkers"
    />

    <div v-if="isAdmin && showRequestList" class="admin-request-popup">
      <h3>등록 요청 리스트</h3>
      <ul>
        <li
          v-for="request in markerRequests"
          :key="request.id"
          @click="fetchRequestDetail(request.id)"
        >
          {{ request.title }} ({{ request.createdAt }})
        </li>
      </ul>
    </div>

    <div v-if="isAdmin && showRequestDetail" class="admin-detail-popup">
      <h3>요청 상세 정보</h3>
      <p>제목: {{ selectedRequest?.title }}</p>
      <p>내용: {{ selectedRequest?.content }}</p>
      <img v-if="selectedRequest?.image" :src="selectedRequest.image" />
      <button @click="approveMarker(selectedRequest?.id)">승인</button>
      <button @click="rejectMarker(selectedRequest?.id)">거부</button>
      <button @click="closeRequestDetail">닫기</button>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, toRaw } from "vue";
import MarkerPopup from "@/components/MarkerPopup.vue";
import apiClient from "@/api/axios";

export default {
  components: { MarkerPopup },
  setup() {
    const isAdmin = ref(localStorage.getItem("isAdmin") === "true");
    const showRequestList = ref(false);
    const showRequestDetail = ref(false);
    const showPopup = ref(false);
    const isDetail = ref(false);
    const selectedMarker = ref(null);
    const selectedRequest = ref(null);
    const currentPosition = ref(null);
    const markers = ref([]);
    const kakaoMarkers = ref([]);
    const map = ref(null);
    const clusterer = ref(null);
    const markerRequests = ref([]);

    const initMap = (center = { lat: 37.5665, lng: 126.978 }) => {
      if (!window.kakao || !window.kakao.maps) return;

      const container = document.getElementById("map");
      const options = {
        center: new window.kakao.maps.LatLng(center.lat, center.lng),
        level: 3,
      };
      map.value = new window.kakao.maps.Map(container, options);

      fetchMarkers();

      window.kakao.maps.event.addListener(toRaw(map.value), "click", (mouseEvent) => {
        const lat = mouseEvent.latLng.getLat();
        const lon = mouseEvent.latLng.getLng();

        if (lat < 33.0 || lat > 39.5 || lon < 124.5 || lon > 131.0) {
          alert("대한민국 영토 범위 내에서만 등록할 수 있습니다.");
          return;
        }

        const geocoder = new window.kakao.maps.services.Geocoder();
        geocoder.coord2Address(lon, lat, (result, status) => {
          if (status !== window.kakao.maps.services.Status.OK) {
            alert("주소를 확인할 수 없는 위치입니다.");
            return;
          }

          const address = result[0]?.address?.address_name || "";
          if (!address) {
            alert("주소가 존재하지 않는 위치입니다.");
            return;
          }

          currentPosition.value = { latitude: lat, longitude: lon };
          selectedMarker.value = null;
          isDetail.value = false;
          showPopup.value = true;
        });
      });
    };

    const fetchMarkers = async () => {
      try {
        const response = await apiClient.get("/markers/approve");
        markers.value = response.data;
        displayMarkers();
      } catch (e) {
        console.error("🚨 마커 불러오기 실패:", e);
      }
    };

    const displayMarkers = () => {
      if (clusterer.value) {
        toRaw(clusterer.value).setMap(null);
      }

      const validMarkers = markers.value
        .filter((m) => isFinite(m.latitude) && isFinite(m.longitude))
        .map((m, i) => {
          try {
            const latlng = new window.kakao.maps.LatLng(m.latitude, m.longitude);
            const marker = new window.kakao.maps.Marker({ position: latlng });

            window.kakao.maps.event.addListener(marker, "click", () =>
              fetchMarkerDetail(m.id)
            );

            return marker;
          } catch (err) {
            console.warn(`❌ 마커 ${i} 생성 실패:`, m, err);
            return null;
          }
        })
        .filter((m) => m instanceof window.kakao.maps.Marker);

      kakaoMarkers.value = validMarkers;

      clusterer.value = new window.kakao.maps.MarkerClusterer({
        map: toRaw(map.value),
        averageCenter: true,
        minLevel: 5,
      });

      if (validMarkers.length > 0) {
        toRaw(clusterer.value).addMarkers(validMarkers);
      }
    };

    const requestUserLocation = () => {
      if ("geolocation" in navigator) {
        navigator.geolocation.getCurrentPosition(
          (pos) => initMap({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
          () => initMap({ lat: 37.5665, lng: 126.978 })
        );
      } else {
        initMap({ lat: 37.5665, lng: 126.978 });
      }
    };

    const fetchMarkerDetail = async (id) => {
      try {
        const res = await apiClient.get(`/markers/${id}`);
        selectedMarker.value = res.data;
        isDetail.value = true;
        showPopup.value = true;
      } catch (e) {
        console.error("🚨 마커 상세 조회 실패:", e);
      }
    };

    const fetchRequestDetail = async (id) => {
      try {
        const res = await apiClient.get(`/markers/requests/${id}`);
        selectedRequest.value = res.data;
        showRequestDetail.value = true;
      } catch (e) {
        console.error("🚨 요청 상세 조회 실패:", e);
      }
    };

    const approveMarker = async (id) => {
      try {
        await apiClient.put(`/markers/approve/${id}`);
        alert("마커가 승인되었습니다.");
        fetchMarkers();
        showRequestDetail.value = false;
      } catch (e) {
        console.error("🚨 마커 승인 실패:", e);
      }
    };

    const rejectMarker = async (id) => {
      try {
        await apiClient.delete(`/markers/${id}`);
        alert("마커가 거부되었습니다.");
        fetchMarkers();
        showRequestDetail.value = false;
      } catch (e) {
        console.error("🚨 마커 거부 실패:", e);
      }
    };

    const closePopup = () => {
      showPopup.value = false;
      selectedMarker.value = null;
    };

    const closeRequestDetail = () => {
      showRequestDetail.value = false;
      selectedRequest.value = null;
    };

    const fetchMarkerRequests = async () => {
      if (!isAdmin.value) return;
      try {
        const res = await apiClient.get("/markers/requests");
        markerRequests.value = res.data;
        showRequestList.value = true;
      } catch (e) {
        console.error("🚨 관리자 요청 리스트 불러오기 실패:", e);
      }
    };

    onMounted(() => {
      if (isAdmin.value) fetchMarkerRequests();
      requestUserLocation();
    });

    return {
      isAdmin,
      showRequestList,
      showRequestDetail,
      markers,
      fetchMarkers,
      displayMarkers,
      selectedMarker,
      fetchMarkerDetail,
      closePopup,
      showPopup,
      isDetail,
      selectedRequest,
      fetchRequestDetail,
      approveMarker,
      rejectMarker,
      closeRequestDetail,
      currentPosition,
      markerRequests,
      fetchMarkerRequests,
    };
  },
};
</script>

<style>
.map-container {
  width: 100%;
  height: 80vh;
}

.admin-request-popup,
.admin-detail-popup {
  position: absolute;
  top: 10px;
  right: 10px;
  background: white;
  padding: 10px;
  border-radius: 8px;
  z-index: 1000;
}
</style>
