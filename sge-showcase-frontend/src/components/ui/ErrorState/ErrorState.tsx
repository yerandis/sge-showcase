interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export default function ErrorState({ message = 'No se pudieron cargar los datos.', onRetry }: ErrorStateProps) {
  return (
    <div style={{
      display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '0.75rem',
      padding: '2.5rem 1rem', textAlign: 'center', color: 'var(--color-text-secondary)',
    }}>
      <p style={{ color: 'var(--color-danger)', fontWeight: 500 }}>{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          style={{
            padding: '0.5rem 1rem', border: '1px solid var(--color-border)',
            background: 'var(--color-bg-primary)', borderRadius: 'var(--radius-md)', cursor: 'pointer',
          }}
        >
          Reintentar
        </button>
      )}
    </div>
  );
}