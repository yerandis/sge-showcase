// ═══════════════════════════════════════════════════════════════
// TIPOS DEL SISTEMA DE GESTIÓN DE EMPLEADOS
// Estos tipos reflejan exactamente la estructura de datos
// que devuelve el backend (EmployeeResponse.java, etc.)
// ═══════════════════════════════════════════════════════════════

import type { Department } from "./department.types";

/**
 * Los dos posibles estados de un empleado.
 * Coincide con el enum EmployeeStatus.java del backend.
 *
 * 'ACTIVE' | 'INACTIVE' significa: solo puede ser uno de estos dos valores.
 */
export type EmployeeStatus = 'ACTIVE' | 'INACTIVE';

/**
 * Representa un empleado tal como lo devuelve la API.
 * Coincide con EmployeeResponse.java del backend.
 *
 *  'department' es un objeto Department completo. 
 *  El backend ya hace el join.
 */
export interface Employee {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string | null;
  position: string;
  department: Department;
  salary: number;
  status: EmployeeStatus;
  hireDate: string;       // ISO date: "2024-01-15"
  createdAt: string;      // ISO datetime: "2024-01-15T10:30:00"
  updatedAt: string;
}

/**
 * Los datos que enviamos al backend para crear o actualizar un empleado.
 * Coincide con EmployeeRequest.java del backend.
 *
 * Nota: aquí 'departmentId' es un number (el ID), no un Department completo.
 * El backend solo necesita el ID para hacer el JOIN internamente.
 */
export interface EmployeeFormData {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  position: string;
  departmentId: string | '';   // '' para el estado inicial del select vacío
  salary: number | '';         // '' para el estado inicial del input vacío
  status: EmployeeStatus | '';
  hireDate: string;
}

/**
 * Parámetros para buscar/filtrar empleados.
 * Se convierten en query params de la URL.
 */
export interface EmployeeFilters {
  search?:        string;
  status?:        EmployeeStatus | '';
  departmentId?:  string | '';
  page:           number;
  size:           number;
  sortBy:         string;
  sortDir:        'asc' | 'desc';
}

/**
 * Estadísticas para el Dashboard.
 * Coincide con lo que devuelve /api/v1/employees/dashboard/stats
 */
export interface DashboardStats {
  totalEmployees: number;
  activeEmployees: number;
  inactiveEmployees: number;
  totalDepartments: number;
}
