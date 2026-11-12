import { useEffect, useState } from 'react';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer } from 'recharts';import { formatCurrency } from '../../../utils/formatters';
import Spinner from '../../../components/ui/Spinner/Spinner';
import styles from './../OverviewPage.module.css';
import { type DepartmentAverageSalary, getAverageSalaryByDepartment } from '../../../services/analytics/analyticsService';

export default function DepartmentsAnalyticsPage() {
  const [data, setData] = useState<DepartmentAverageSalary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getAverageSalaryByDepartment()
      .then(setData)
      .catch(() => setError('No se pudieron cargar los datos de departamentos.'))
      .finally(() => setIsLoading(false));
  }, []);

  if (isLoading) return <Spinner message="Cargando análisis de departamentos..." />;

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <h1 className={styles.pageTitle}>Análisis de departamentos</h1>
        <p className={styles.pageSubtitle}>Salario promedio por departamento (empleados activos)</p>
      </div>

      {error && <p style={{ color: 'var(--color-danger)' }}>{error}</p>}

      <div className={styles.chartCard}>
        <div className={styles.chartHeader}>
          <h2 className={styles.chartTitle}>Salario promedio por departamento</h2>
        </div>
        <div className={styles.chartBody}>
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={data} margin={{ top: 8, right: 8, left: -16, bottom: 0 }}>
              <XAxis dataKey="name" tick={{ fontSize: 11 }} />
              <YAxis tick={{ fontSize: 11 }} />
              <Tooltip formatter={(value) => [formatCurrency(Number(value)), 'Promedio']} />
              <Bar dataKey="averageSalary" fill="#7c3aed" radius={[4, 4, 0, 0]} />
            </BarChart>
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
  );
}