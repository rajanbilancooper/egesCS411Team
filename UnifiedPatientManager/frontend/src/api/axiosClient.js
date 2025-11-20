import axios from "axios";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL, // should be "http://localhost:8080"
});
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("accessToken");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Log outgoing request for debugging
  console.debug("Axios request", { method: config.method, url: config.baseURL + config.url });
  return config;
});

// Add a response interceptor for error logging
client.interceptors.response.use(
  (res) => res,
  (error) => {
    console.error("Axios error response", error?.response?.status, error?.response?.data?.message || error?.message);
    return Promise.reject(error);
  }
);

export default client;
