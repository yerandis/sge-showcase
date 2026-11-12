import axios from "axios";
import {
  getAccessToken,
  refreshAccessToken,
  clearSession,
} from "./authService";

// const apiClient = axios.create({
//   baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
//   headers: { 'Content-Type': 'application/json' },
//   timeout: 10000,
// });
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  headers: {
    "Content-Type": "application/json",
    "ngrok-skip-browser-warning": "true",
  },
  timeout: 10000,
});

// ── Interceptor de REQUEST: adjunta el token JWT ──────────────────
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ── Interceptor de RESPONSE: maneja token expirado ────────────────
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (token: string) => void;
  reject: (error: unknown) => void;
}> = [];

function processQueue(error: unknown, token: string | null) {
  failedQueue.forEach(({ resolve, reject }) => {
    if (error) reject(error);
    else resolve(token!);
  });
  failedQueue = [];
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 403) {
      // Aquí usa tu sistema de notificaciones (ya tienes NotificationToast)
      window.dispatchEvent(
        new CustomEvent("app:forbidden", {
          detail:
            error.response.data?.message ??
            "No tienes permisos para esta acción",
        }),
      );
    }
    // Si es 401 y no es un retry y no es la ruta de login
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !originalRequest.url?.includes("/auth/")
    ) {
      if (isRefreshing) {
        // Si ya hay un refresh en curso, encolar la petición
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const newToken = await refreshAccessToken();
        if (newToken) {
          processQueue(null, newToken);
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return apiClient(originalRequest);
        } else {
          // No se pudo renovar: sesión expirada
          processQueue(error, null);
          clearSession();
          window.location.href = "/login";
          return Promise.reject(error);
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        clearSession();
        window.location.href = "/login";
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  },
);

export default apiClient;
