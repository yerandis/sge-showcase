import apiClient from "./axiosConfig";
import type { LoginCredentials, AuthUser } from "../types/auth.types";
import type { ApiResponse } from "../types/common.types";

const TOKEN_KEY = "sge_access_token";
const REFRESH_KEY = "sge_refresh_token";
const USER_KEY = "sge_user";

interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: AuthUser;
}

export async function login(
  credentials: LoginCredentials,
): Promise<AuthResponse> {
  const response = await apiClient.post<ApiResponse<AuthResponse>>(
    "/auth/login",
    credentials,
  );
  const data = response.data.data;
  saveSession(data);
  return data;
}

export async function logout(): Promise<void> {
  try {
    await apiClient.post("/auth/logout");
  } catch {
    // Aunque falle el servidor, limpiamos localmente
  } finally {
    clearSession();
  }
}

export async function refreshAccessToken(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;

  try {
    const response = await apiClient.post<ApiResponse<AuthResponse>>(
      "/auth/refresh",
      { refreshToken },
    );
    const data = response.data.data;
    saveSession(data);
    return data.accessToken;
  } catch {
    clearSession();
    return null;
  }
}

// ── Gestión de sesión en localStorage ────────────────────────────

function saveSession(data: AuthResponse): void {
  localStorage.setItem(TOKEN_KEY, data.accessToken);
  localStorage.setItem(REFRESH_KEY, data.refreshToken);
  localStorage.setItem(USER_KEY, JSON.stringify(data.user));
}

export function clearSession(): void {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(REFRESH_KEY);
  localStorage.removeItem(USER_KEY);
}

export function getAccessToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_KEY);
}

export function getSavedUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw) as AuthUser;
  } catch {
    return null;
  }
}

export function isSessionActive(): boolean {
  return Boolean(getAccessToken() && getSavedUser());
}

export function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    if (!payload.exp) return true;
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

export interface ChangePasswordPayload {
  currentPassword: string;
  newPassword: string;
}

export async function changePassword(
  payload: ChangePasswordPayload,
): Promise<void> {
  await apiClient.patch("/auth/change-password", payload);
}

export async function uploadProfileAvatar(file: File): Promise<string> {
  const formData = new FormData();
  formData.append("file", file);

  // Indicamos que el cuerpo es multipart para que Axios envíe el archivo
  // con el formato que espera el endpoint de Spring Boot.
  const response = await apiClient.post<ApiResponse<{ avatarUrl: string }>>(
    "/auth/profile/avatar",
    formData,
    {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    },
  );

  return response.data.data.avatarUrl;
}

export function updateSavedUser(user: AuthUser): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}
