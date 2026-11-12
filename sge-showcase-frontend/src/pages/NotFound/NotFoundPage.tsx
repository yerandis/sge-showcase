import { Link } from 'react-router-dom';

export default function NotFoundPage() {
  return (
    <div style={{ display:'flex', flexDirection:'column', alignItems:'center',
                  justifyContent:'center', minHeight:'100vh', gap:'1rem', textAlign:'center',
                  padding:'1rem' }}>
      <div style={{ fontSize:'4rem', fontWeight:800, color:'var(--color-primary)' }}>404</div>
      <h1 style={{ fontSize:'1.5rem', fontWeight:700 }}>Página no encontrada</h1>
      <p style={{ color:'var(--color-text-secondary)' }}>
        La ruta que buscas no existe o no tienes acceso a ella.
      </p>
      <Link to="/dashboard"
        style={{ padding:'0.5rem 1.25rem', background:'var(--color-primary)',
                 color:'#fff', borderRadius:'0.5rem', textDecoration:'none',
                 fontWeight:600 }}>
        Ir al dashboard
      </Link>
    </div>
  );
}