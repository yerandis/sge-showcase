// Tipos que reflejan DashboardOverviewResponse.java y DashboardActivityDto.java.

export interface DashboardActivity {
  username: string;
  action: string;
  entityType: string | null;
  status: string;
  createdAt: string;
}

export interface DashboardOverview {
  // null significa que el usuario no tiene acceso a esa métrica.
  totalEmployees: number | null;
  activeEmployees: number | null;
  inactiveEmployees: number | null;
  totalDepartments: number | null;
  hiresLast30Days: number | null;

  // Puede ser null cuando REPORT_VIEW no está autorizado.
  recentActivity: DashboardActivity[] | null;
  visibleSections: string[];
}
