import apiClient from "./axiosConfig";
import type { ApiResponse } from "../types/common.types";
import type { DashboardOverview } from "../types/dashboard.types";

// Solicita el resumen que ya viene filtrado por permisos desde el backend.
export async function getDashboardOverview(): Promise<DashboardOverview> {
  const response = await apiClient.get<ApiResponse<DashboardOverview>>(
    "/dashboard/overview",
  );

  return response.data.data;
}
