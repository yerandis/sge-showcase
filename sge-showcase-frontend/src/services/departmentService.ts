import apiClient from './axiosConfig';
import type { ApiResponse, PageResponse } from '../types/common.types';
import type { Department, DepartmentFilter, DepartmentFormData } from '../types/department.types';
import type { DashboardStats } from '../types/employee.types';


/**
 * Convierte los filtros a query parameters de la URL.
 * Omite los valores vacíos para no enviarlos al backend.
 */
function buildParams(filters: DepartmentFilter | null): Record<string, string | number> {

  if(filters == null) return{page: 0, size: 0, sortBy: '', sortDir: ''};

  const params: Record<string, string | number> = {
    page: filters.page,
    size: filters.size,
    sortBy: filters.sortBy,
    sortDir: filters.sortDir,
  };

  if (filters.search && filters.search.trim() !== '') {
    params.search = filters.search.trim();
  }
  return params; 
}



  //  --- ENDPOINTS --- 

export async function getDepartments(filters: DepartmentFilter): Promise<PageResponse<Department>> {
  const params = buildParams(filters);
  const response = await apiClient.get<ApiResponse<PageResponse<Department>>>('/departments', {params});
  return response.data.data;
}

/**
 * Obtener un Department por ID.
 */
export async function getDepartmentById(id: string): Promise<Department> {
  //  bucar como convertir el id: string en id: UUID para poder enviarlo a la api

  const response = await apiClient.get<ApiResponse<Department>>(`/departments/${id}`);
  return response.data.data;
}

/**
 * Crear un nuevo departments.
 * Devuelve el departments creado (con su ID asignado por la BD).
 */
export async function createDepartment(data: DepartmentFormData): Promise<Department> {
  const response = await apiClient.post<ApiResponse<Department>>('/departments', data);
  return response.data.data;
}

/**
 * Actualizar un departments existente.
 */
export async function updateDepartment(id: string, data: DepartmentFormData): Promise<Department> {
  const response = await apiClient.put<ApiResponse<Department>>(
    `/departments/${id}`,
    data
  );
  return response.data.data;
}

/**
 * Eliminar un departments.
 * El backend devuelve 204 No Content (sin body).
 */
export async function deleteDepartment(id: string): Promise<void> {
  await apiClient.delete(`/departments/${id}`);
}

/**
 * Obtener estadísticas del dashboard.
 */
export async function getDashboardStats(): Promise<DashboardStats> {
  const response = await apiClient.get<ApiResponse<DashboardStats>>(
    '/departments/dashboard/stats'
  );
  return response.data.data;
}

export interface DepartmentHistoryEntry {
  id:       string;
  action:   'CREATE' | 'UPDATE' | 'DELETE';
  username: string;
  changes: {  field:    string; 
              oldValue: string; 
              newValue: string 
            }[];
  createdAt: string;
}

export async function getDepartmentHistory(id: string): Promise<DepartmentHistoryEntry[]> {
  const res = await apiClient.get<ApiResponse<DepartmentHistoryEntry[]>>(
    `/departments/${id}/history`
  );
  return res.data.data;
}