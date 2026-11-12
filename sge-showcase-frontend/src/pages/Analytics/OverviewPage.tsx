import { useState, useEffect } from 'react';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell,
  XAxis, YAxis, Tooltip, ResponsiveContainer, Legend, type PieLabelRenderProps
} from 'recharts';
import Spinner from '../../components/ui/Spinner/Spinner';
import styles from './OverviewPage.module.css';
import { getAnalyticsSummary, getEmployeesByDepartment, getHiresByMonth } from '../../services/analytics/analyticsService';
import { data } from 'react-router-dom';

interface DeptData  { name: string; count: number; }
interface TrendData { month: string; hired: number; }
interface StatusData { name: string; value: number; color: string; }

export default function OverviewPage() {

  const [deptData,   setDeptData]   = useState<DeptData[]>([]);
  const [trendData,  setTrendData]  = useState<TrendData[]>([]);
  const [statusData, setStatusData] = useState<StatusData[]>([]);
  const [isLoading,  setIsLoading]  = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [summary, departments, hires] = await Promise.all([
          getAnalyticsSummary(),
          getEmployeesByDepartment(),
          getHiresByMonth(12),
        ]);

        setStatusData([
          { name: 'Activos',   value: summary.activeEmployees,   color: '#16a34a' },
          { name: 'Inactivos', value: summary.inactiveEmployees, color: '#94a3b8' },
        ]);

        setDeptData(departments.map(d => ({ name: d.name, count: d.employeeCount })));

        setTrendData(hires.map(h => ({ month: h.month, hired: h.hires })));
      } catch (e) {
        console.error(e);
      } finally {
        setIsLoading(false);
      }
  }
  load();
  }, []);

  if (isLoading) return <Spinner message="Cargando analítica..." />;

  // const total = statusData.reduce((acc, d) => acc + d.value, 0);

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Resumen de Analítica</h1>
        <p className={styles.pageSubtitle}>Visión general de los datos de la organización</p>
      </div>

      <div className={styles.chartsGrid}>

        {/* Gráfico 1: Empleados por departamento */}
        <div className={styles.chartCard}>
          <div className={styles.chartHeader}>
            <h2 className={styles.chartTitle}>Empleados por Departamento</h2>
          </div>
          <div className={styles.chartBody}>
            <ResponsiveContainer width="100%" height={240}>
              <BarChart data={deptData} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
                <XAxis dataKey="name" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                <Tooltip
                  formatter={(value) => [`${value ?? 0} empleados`, 'Total']}
                  cursor={{ fill: 'rgba(124,58,237,0.08)' }}
                />
                <Bar dataKey="count" fill="#7c3aed" radius={[4,4,0,0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Gráfico 2: Distribución activos/inactivos */}
        <div className={styles.chartCard}>
          <div className={styles.chartHeader}>
            <h2 className={styles.chartTitle}>Distribución de Plantilla</h2>
          </div>
          <div className={styles.chartBody}>
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie
                  data={statusData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={3}
                  dataKey="value"
                  label={({ name, percent }: PieLabelRenderProps) =>
                    `${name} ${((percent ?? 0) * 100).toFixed(0)}%`
                  }
                >
                  {statusData.map((entry, i) => (
                    <Cell key={i} fill={entry.color} />
                  ))}
                </Pie>
              <Tooltip
                formatter={(value) => [`${value ?? 0} empleados`]}
              />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Gráfico 3: Tendencia de contratación */}
        <div className={`${styles.chartCard} ${styles.chartCardWide}`}>
          <div className={styles.chartHeader}>
            <h2 className={styles.chartTitle}>Tendencia de Contratación (12 meses)</h2>
          </div>
          <div className={styles.chartBody}>
            <ResponsiveContainer width="100%" height={240}>
              <LineChart data={trendData} margin={{ top: 8, right: 16, left: -16, bottom: 0 }}>
                <XAxis dataKey="month" tick={{ fontSize: 11 }} />
                <YAxis tick={{ fontSize: 11 }} allowDecimals={false} />
                <Tooltip formatter={(value) => [`${value ?? 0} contrataciones`]} />
                <Line
                  type="monotone" dataKey="hired"
                  stroke="#7c3aed" strokeWidth={2.5}
                  dot={{ r: 4, fill: '#7c3aed' }}
                  activeDot={{ r: 6 }}
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {data.length === 0 && !isLoading && (
          <div style={{ display:'flex', flexDirection:'column', alignItems:'center',
                        justifyContent:'center', height:'200px', color:'var(--color-text-secondary)',
                        gap:'0.5rem' }}>
            <svg style={{ width:'2rem', height:'2rem' }} fill="none" viewBox="0 0 24 24"
                stroke="currentColor" strokeWidth={1.5}>
              <path strokeLinecap="round" strokeLinejoin="round"
                d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75z" />
            </svg>
            <span>Sin datos disponibles</span>
          </div>
        )}

      </div>
    </div>
  );
}