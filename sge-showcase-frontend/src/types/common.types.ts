/**
 * Respuesta paginada del backend.
 * Coincide con PageResponse.java del backend.
 *
 * Generic <T>: igual que en Java, T puede ser cualquier tipo.
 * PageResponse<Employee> significa: una página de Employees.
 * PageResponse<Department> sería una página de Departments.
 *
 * Comparación Java:
 * public class PageResponse<T> { ... }
 * TypeScript:
 * interface PageResponse<T> { ... }
 */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/**
 * La envoltura estándar de todas las respuestas de la API.
 * Coincide con ApiResponse.java del backend.
 */
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}


/**
 * Errores de validación que devuelve el backend cuando un campo es inválido.
 * Es el 'data' de ApiResponse cuando hay un error 400.
 *
 * Record<string, string>: un objeto donde las claves son strings (nombres de campo)
 * y los valores son strings (mensajes de error).
 * Ejemplo: { "email": "El email no es válido", "salary": "Debe ser positivo" }
 *
 * 🔄 Comparación Java: equivalente a Map<String, String>
 */
export type ValidationErrors = Record<string, string>;