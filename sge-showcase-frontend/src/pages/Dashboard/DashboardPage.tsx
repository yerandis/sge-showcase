import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
// import { getDashboardStats } from '../../services/employeeService';
// import type { DashboardStats } from '../../types/employee.types';
import { getDashboardOverview } from "../../services/dashboardService";
import type { DashboardOverview } from "../../types/dashboard.types";
import StatCard from "./StatCard";
import styles from "./DashboardPage.module.css";
import Can from "../../components/auth/Can";
import DashboardCharts from "./DashboardCharts";

/**
 * DashboardPage: primera página que ve el usuario.
 * Muestra un resumen del estado actual del sistema.
 *
 * Flujo de datos:
 * 1. Componente se monta
 * 2. useEffect dispara la llamada a la API
 * 3. Mientras espera: stats=null → las tarjetas muestran skeleton
 * 4. API responde: setStats(data) → React re-renderiza con datos reales
 * 5. Si hay error: setError(message) → se muestra el mensaje de error
 */
export default function DashboardPage() {
  /**
   * Estado del componente:
   * - stats: los datos del backend. null = aún no cargados.
   * - isLoading: true mientras espera la respuesta de la API.
   * - error: mensaje de error si la llamada falla.
   */
  // const [stats, setStats] = useState<DashboardStats | null>(null);
  // const [isLoading, setIsLoading] = useState(true);
  // const [error, setError] = useState<string | null>(null);

  // El backend devuelve únicamente las métricas autorizadas para este usuario.
  const [overview, setOverview] = useState<DashboardOverview | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  /**
   * useEffect con array de dependencias vacío []:
   * Se ejecuta EXACTAMENTE UNA VEZ, cuando el componente se monta.
   * Es el lugar correcto para llamadas a API que cargan datos iniciales.
   *
   * La función interna es async porque await no puede usarse
   * directamente en el callback de useEffect.
   */
  useEffect(() => {
    // fetchStats();

    fetchOverview();
  }, []); // [] = solo al montar, nunca más

  // async function fetchStats() {
  //   try {
  //     setIsLoading(true);
  //     setError(null);
  //     const data = await getDashboardStats();
  //     setStats(data);
  //   } catch (err) {
  //     setError(
  //       "No se pudieron cargar las estadísticas. Verifica que el backend esté corriendo.",
  //     );
  //     console.error("Error cargando dashboard: ", err);
  //   } finally {
  //     // finally siempre se ejecuta, tanto si hubo éxito como si hubo error
  //     setIsLoading(false);
  //   }
  // }
  async function fetchOverview() {
    try {
      setIsLoading(true);
      setError(null);

      const data = await getDashboardOverview();
      setOverview(data);
    } catch (err) {
      setError("No se pudo cargar el resumen operativo.");
      console.error("Error cargando dashboard:", err);
    } finally {
      setIsLoading(false);
    }
  }

  /**
   * Porcentaje de empleados activos para la barra de progreso.
   * Si stats es null o total es 0, devuelve 0.
   */
  // const activePercentage =
  //   stats && stats.totalEmployees > 0
  //     ? Math.round((stats.activeEmployees / stats.totalEmployees) * 100)
  //     : 0;
  // No se calcula el porcentaje si el usuario no puede ver las métricas.
  const activePercentage =
    overview?.totalEmployees && overview.totalEmployees > 0
      ? Math.round(
          ((overview.activeEmployees ?? 0) / overview.totalEmployees) * 100,
        )
      : 0;

  return (
    <div className={styles.page}>
      {/* ── CABECERA ─────────────────────────────────────────── */}
      {/* <div className={styles.pageHeader}>
        <div>
          <h1 className={styles.pageTitle}>Dashboard</h1>
          <p className={styles.pageSubtitle}>
            Resumen operativo del sistema de gestión de empleados
          </p>
        </div>

        <button
          type="button"
          className={styles.refreshBtn}
          onClick={fetchOverview}
          disabled={isLoading}
        >
          <svg
            className={styles.refreshIcon}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
          {isLoading ? "Cargando..." : "Actualizar"}
        </button>
      </div> */}

      {/* Cabecera principal: identidad, contexto y acción */}
      <div className={styles.pageHeader}>
        <div className={styles.headerCopy}>
          <span className={styles.pageEyebrow}>
            SGE <span className={styles.eyebrowDivider} /> CENTRO DE OPERACIONES
          </span>

          <h1 className={styles.pageTitle}>Resumen ejecutivo</h1>

          <p className={styles.pageSubtitle}>
            Una visión clara de la plantilla y de la actividad de tu
            organización.
          </p>

          <div className={styles.headerMeta}>
            <span className={styles.statusDot} aria-hidden="true" />
            <span>Información disponible según tus permisos</span>
          </div>
        </div>

        <button
          type="button"
          className={styles.refreshBtn}
          onClick={fetchOverview}
          disabled={isLoading}
          aria-label="Actualizar el resumen operativo"
        >
          <svg
            className={`${styles.refreshIcon} ${isLoading ? styles.refreshing : ""}`}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
            />
          </svg>
          {isLoading ? "Actualizando…" : "Actualizar datos"}
        </button>
      </div>

      {/* ── MENSAJE DE ERROR ─────────────────────────────────── */}
      {error && (
        <div className={styles.errorBox} role="alert">
          <svg
            className={styles.errorIcon}
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
            strokeWidth={2}
            aria-hidden="true"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"
            />
          </svg>
          <span>{error}</span>
        </div>
      )}

      {/* ── TARJETAS DE ESTADÍSTICAS ─────────────────────────── */}
      {/*
      Durante la carga mostramos skeletons.
      Cuando llega la respuesta, cada grupo aparece solo si el backend
      incluyó su sección en visibleSections.
    */}
      {(isLoading ||
        overview?.visibleSections.includes("employees") ||
        overview?.visibleSections.includes("departments") ||
        overview?.visibleSections.includes("analytics")) && (
        <div className={styles.statsGrid}>
          {/* EMPLEADOS: solo con EMPLOYEE_READ */}
          {(isLoading || overview?.visibleSections.includes("employees")) && (
            <>
              <StatCard
                label="Total empleados"
                value={isLoading ? null : (overview?.totalEmployees ?? null)}
                accentColor="var(--dashboard-accent-blue)"
                iconBgColor="var(--dashboard-icon-blue)"
                footer="Registrados en el sistema"
                icon={
                  <svg
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"
                    />
                  </svg>
                }
              />

              <StatCard
                label="Empleados activos"
                value={isLoading ? null : (overview?.activeEmployees ?? null)}
                accentColor="#16A34A"
                iconBgColor="#F0FDF4"
                footer="En plantilla actualmente"
                icon={
                  <svg
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
                    />
                  </svg>
                }
              />

              <StatCard
                label="Empleados inactivos"
                value={isLoading ? null : (overview?.inactiveEmployees ?? null)}
                accentColor="#D97706"
                iconBgColor="#FFFBEB"
                footer="Dados de baja"
                icon={
                  <svg
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                    strokeWidth={2}
                    aria-hidden="true"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728L5.636 5.636"
                    />
                  </svg>
                }
              />
            </>
          )}

          {/* DEPARTAMENTOS: solo con DEPARTMENT_READ */}
          {(isLoading || overview?.visibleSections.includes("departments")) && (
            <StatCard
              label="Departamentos"
              value={isLoading ? null : (overview?.totalDepartments ?? null)}
              accentColor="#7C3AED"
              iconBgColor="#F5F3FF"
              footer="Áreas de la empresa"
              icon={
                <svg
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth={2}
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M3.75 21h16.5M4.5 3h15M5.25 3v18m13.5-18v18M9 6.75h1.5m-1.5 3h1.5m-1.5 3h1.5m3-6H15m-1.5 3H15m-1.5 3H15M9 21v-3.375c0-.621.504-1.125 1.125-1.125h3.75c.621 0 1.125.504 1.125 1.125V21"
                  />
                </svg>
              }
            />
          )}

          {/* ANALÍTICA: solo con REPORT_VIEW */}
          {(isLoading || overview?.visibleSections.includes("analytics")) && (
            <StatCard
              label="Contrataciones recientes"
              value={isLoading ? null : (overview?.hiresLast30Days ?? null)}
              accentColor="#0891B2"
              iconBgColor="#ECFEFF"
              footer="Últimos 30 días"
              icon={
                <svg
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke="currentColor"
                  strokeWidth={2}
                  aria-hidden="true"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    d="M8 7V3m8 4V3M4 11h16M5 5h14a1 1 0 011 1v13H4V6a1 1 0 011-1z"
                  />
                </svg>
              }
            />
          )}
        </div>
      )}
      {/* Analítica adicional: solo para usuarios autorizados */}
      {overview?.visibleSections.includes("analytics") && <DashboardCharts />}

      <div className={styles.insightsGrid}>
        {/* ── DISTRIBUCIÓN DE LA PLANTILLA ──────────────────────── */}
        {overview?.visibleSections.includes("employees") && (
          <section className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>
                Distribución de la plantilla
              </h2>

              <Can permission="EMPLOYEE_READ">
                <Link to="/employees" className={styles.sectionLink}>
                  Ver todos los empleados →
                </Link>
              </Can>
            </div>

            <div className={styles.sectionBody}>
              <div className={styles.progressLabel}>
                <span>Empleados activos</span>
                <span>
                  {overview.activeEmployees ?? 0} de{" "}
                  {overview.totalEmployees ?? 0} ({activePercentage}%)
                </span>
              </div>

              <div
                className={styles.progressBar}
                role="progressbar"
                aria-label="Porcentaje de empleados activos"
                aria-valuemin={0}
                aria-valuemax={100}
                aria-valuenow={activePercentage}
              >
                <div
                  className={styles.progressFill}
                  style={{ width: `${activePercentage}%` }}
                />
              </div>
            </div>
          </section>
        )}

        {/* ── ACTIVIDAD RECIENTE: solo con REPORT_VIEW ──────────── */}
        {overview?.visibleSections.includes("recentActivity") && (
          <section className={styles.section}>
            <div className={styles.sectionHeader}>
              <h2 className={styles.sectionTitle}>Actividad reciente</h2>
            </div>

            <div className={`${styles.sectionBody} ${styles.activityScroll}`}>
              {overview.recentActivity && overview.recentActivity.length > 0 ? (
                <ul className={styles.activityList}>
                  {overview.recentActivity.map((activity, index) => (
                    <li
                      key={`${activity.createdAt}-${activity.username}-${index}`}
                      className={styles.activityItem}
                    >
                      <div className={styles.activityMarker} aria-hidden="true">
                        <span />
                      </div>

                      <div className={styles.activityMain}>
                        <div className={styles.activityTitleRow}>
                          <strong className={styles.activityTitle}>
                            {activity.action}
                          </strong>

                          <span
                            className={styles.activityStatus}
                            data-status={(
                              activity.status || "unknown"
                            ).toLowerCase()}
                          >
                            {activity.status || "Registrado"}
                          </span>
                        </div>

                        <p className={styles.activityMeta}>
                          {activity.entityType ?? "Actividad"} ·{" "}
                          {activity.username}
                        </p>

                        <time
                          className={styles.activityTime}
                          dateTime={activity.createdAt}
                        >
                          {new Date(activity.createdAt).toLocaleString(
                            "es-PR",
                            {
                              day: "2-digit",
                              month: "short",
                              year: "numeric",
                              hour: "2-digit",
                              minute: "2-digit",
                            },
                          )}
                        </time>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.pageSubtitle}>
                  No hay actividad reciente disponible.
                </p>
              )}
            </div>
          </section>
        )}
      </div>
      {/* Estado vacío cuando el usuario no tiene secciones autorizadas. */}
      {!isLoading &&
        !error &&
        overview &&
        overview.visibleSections.length === 0 && (
          <section className={styles.section}>
            <div className={styles.sectionBody}>
              <p className={styles.pageSubtitle}>
                No tienes permisos para visualizar las métricas del dashboard.
              </p>
            </div>
          </section>
        )}
    </div>
  );
}
