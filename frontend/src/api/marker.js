import axios from "axios";

const API_URL = "/markers";

export const getMarkers = () => axios.get(API_URL);
export const addMarker = (markerData) => axios.post(API_URL, markerData);
