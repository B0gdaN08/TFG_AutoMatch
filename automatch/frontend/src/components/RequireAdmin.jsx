import { Link } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function RequireAdmin({ children }) {
  const { isAdmin } = useAuth();

  if (!isAdmin) {
    return (
      <section>
        <p className="eyebrow">Administración</p>
        <h1>Acceso restringido</h1>
        <div className="card empty-card">
          <p>Esta sección es solo para administradores.</p>
          <Link className="btn btn-primary" to="/catalogo">Volver al catálogo</Link>
        </div>
      </section>
    );
  }

  return children;
}
