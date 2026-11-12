import { useEffect, useState } from "react";
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";

import {
  getEmployeesByDepartment,
  getHiresByMonth,
  type DepartmentHeadcount,
  type MonthlyHire,
} from "../../services/analytics/analyticsService";

import styles from "./DashboardCharts.module.css";

export default function DashboardCharts() {
  const [departments, setDepartments] = useState<DepartmentHeadcount[]>([]);
  const [hires, setHires] = useState<MonthlyHire[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    let cancelled = false;

    async function loadCharts() {
      try {
        setIsLoading(true);
        setHasError(false);

        const [departmentData, hireData] = await Promise.all([
          getEmployeesByDepartment(),
          getHiresByMonth(12),
        ]);

        if (cancelled) return;

        setDepartments(departmentData);
        setHires(hireData);
      } catch (error) {
        if (!cancelled) {
          setHasError(true);
          console.error("Error cargando gráficas del dashboard:", error);
        }
      } finally {
        if (!cancelled) setIsLoading(false);
      }
    }

    loadCharts();

    return () => {
      cancelled = true;
    };
  }, []);

  const formattedHires = hires.map((item) => {
    const [year, month] = item.month.split("-");
    const label = new Intl.DateTimeFormat("es-PR", {
      month: "short",
      year: "2-digit",
    }).format(new Date(Number(year), Number(month) - 1, 1));

    return { ...item, label };
  });

  if (isLoading) {
    return (
      <section className={styles.chartGrid} aria-label="Analítica operativa">
        {[1, 2].map((item) => (
          <div className={styles.chartCard} key={item}>
            <div className={styles.chartSkeletonTitle} />
            <div className={styles.chartSkeleton} />
          </div>
        ))}
      </section>
    );
  }

  if (hasError) {
    return (
      <section className={styles.chartNotice} role="status">
        <strong>Las gráficas no están disponibles ahora.</strong>
        <span>Las demás métricas del dashboard siguen disponibles.</span>
      </section>
    );
  }

  return (
    <section className={styles.chartGrid} aria-label="Analítica operativa">
      <article className={styles.chartCard}>
        <header className={styles.chartHeader}>
          <div>
            <span className={styles.chartEyebrow}>ESTRUCTURA</span>
            <h2 className={styles.chartTitle}>Plantilla por departamento</h2>
          </div>
          <span className={styles.chartCount}>{departments.length} áreas</span>
        </header>

        {departments.length > 0 ? (
          <div className={styles.chartBody}>
            <ResponsiveContainer width="100%" height={250}>
              <BarChart
                data={departments}
                layout="vertical"
                margin={{ top: 4, right: 12, bottom: 4, left: 8 }}
              >
                <CartesianGrid
                  horizontal={false}
                  stroke="var(--color-border)"
                  strokeDasharray="3 3"
                />
                <XAxis
                  type="number"
                  allowDecimals={false}
                  tick={{ fontSize: 11, fill: "var(--color-text-secondary)" }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  type="category"
                  dataKey="name"
                  width={100}
                  tick={{ fontSize: 11, fill: "var(--color-text-secondary)" }}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  formatter={(value) => [
                    `${value ?? 0} empleados`,
                    "Plantilla",
                  ]}
                  contentStyle={{
                    background: "var(--color-bg-primary)",
                    borderColor: "var(--color-border)",
                    borderRadius: "10px",
                    color: "var(--color-text-primary)",
                  }}
                />
                <Bar
                  dataKey="employeeCount"
                  fill="var(--dashboard-accent-violet)"
                  radius={[0, 5, 5, 0]}
                  maxBarSize={22}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <p className={styles.chartEmpty}>No hay datos de departamentos.</p>
        )}
      </article>

      <article className={styles.chartCard}>
        <header className={styles.chartHeader}>
          <div>
            <span className={styles.chartEyebrow}>EVOLUCIÓN</span>
            <h2 className={styles.chartTitle}>Contrataciones por mes</h2>
          </div>
          <span className={styles.chartCount}>12 meses</span>
        </header>

        {formattedHires.length > 0 ? (
          <div className={styles.chartBody}>
            <ResponsiveContainer width="100%" height={250}>
              <LineChart
                data={formattedHires}
                margin={{ top: 12, right: 12, bottom: 4, left: -16 }}
              >
                <CartesianGrid
                  stroke="var(--color-border)"
                  strokeDasharray="3 3"
                  vertical={false}
                />
                <XAxis
                  dataKey="label"
                  tick={{ fontSize: 11, fill: "var(--color-text-secondary)" }}
                  axisLine={false}
                  tickLine={false}
                />
                <YAxis
                  allowDecimals={false}
                  tick={{ fontSize: 11, fill: "var(--color-text-secondary)" }}
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip
                  formatter={(value) => [
                    `${value ?? 0} contrataciones`,
                    "Total",
                  ]}
                  contentStyle={{
                    background: "var(--color-bg-primary)",
                    borderColor: "var(--color-border)",
                    borderRadius: "10px",
                    color: "var(--color-text-primary)",
                  }}
                />
                <Line
                  type="monotone"
                  dataKey="hires"
                  stroke="var(--dashboard-accent-blue)"
                  strokeWidth={2.5}
                  dot={{
                    r: 3,
                    fill: "var(--dashboard-accent-blue)",
                    strokeWidth: 0,
                  }}
                  activeDot={{ r: 5 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        ) : (
          <p className={styles.chartEmpty}>
            No hay contrataciones registradas en el período.
          </p>
        )}
      </article>
    </section>
  );
}
