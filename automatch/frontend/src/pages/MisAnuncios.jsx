import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { request } from '../api';
import { useAuth } from '../AuthContext';
import CarCard from '../components/CarCard';
import ConfirmDialog from '../components/ConfirmDialog';
import { ListIcon } from '../components/icons';

export default function MisAnuncios() {
  const { user } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [pendingRemove, setPendingRemove] = useState(null);

  useEffect(() => {
    if (!user) return;
    request('/cars/mine')
      .then(setCars)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [user]);

  async function remove(car) {
    try {
      await request(`/cars/${car.id}`, { method: 'DELETE' });
      setCars((prev) => prev.filter((c) => c.id !== car.id));
    } catch (e) {
      setError(e.message);
    } finally {
      setPendingRemove(null);
    }
  }

  if (!user) {
    return (
      <section>
        <p className="eyebrow">Mis anuncios</p>
        <h1>Tus coches en venta</h1>
        <div className="card empty-card">
          <span className="icon-tile"><ListIcon /></span>
          <p>Para gestionar tus anuncios necesitas una cuenta.</p>
          <div className="detail-actions">
            <Link className="btn btn-primary" to="/login">Entrar</Link>
            <Link className="btn btn-ghost" to="/registro">Crear cuenta</Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section>
      <p className="eyebrow">Mis anuncios</p>
      <h1>Tus coches en venta</h1>
      {location.state?.justPublished && (
        <p className="alert alert-info">
          Tu anuncio se ha publicado y está pendiente de revisión por un administrador.
        </p>
      )}
      {location.state?.justEdited && (
        <p className="alert alert-info">
          Cambios guardados. Tu anuncio ha vuelto a quedar pendiente de revisión.
        </p>
      )}
      {error && <p className="alert">{error}</p>}
      {loading && <p className="muted">Cargando tus anuncios…</p>}
      {!loading && cars.length === 0 && (
        <div className="card empty-card">
          <span className="icon-tile"><ListIcon /></span>
          <p>Todavía no has publicado ningún coche.</p>
          <div className="detail-actions">
            <Link className="btn btn-primary" to="/vender">Publica el tuyo</Link>
          </div>
        </div>
      )}
      <div className="grid">
        {cars.map((car) => (
          <CarCard
            key={car.id}
            car={car}
            onRemove={setPendingRemove}
            removeLabel="Borrar anuncio"
            onEdit={(c) => navigate(`/mis-anuncios/${c.id}/editar`)}
          />
        ))}
      </div>
      <ConfirmDialog
        open={!!pendingRemove}
        title="Borrar anuncio"
        message={pendingRemove ? `¿Seguro que quieres borrar el anuncio de ${pendingRemove.brand} ${pendingRemove.model}? Esta acción no se puede deshacer.` : ''}
        confirmLabel="Borrar anuncio"
        danger
        onConfirm={() => remove(pendingRemove)}
        onCancel={() => setPendingRemove(null)}
      />
    </section>
  );
}
