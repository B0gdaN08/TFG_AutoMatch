import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { request } from '../api';
import { useAuth } from '../AuthContext';
import CarCard from '../components/CarCard';
import { HeartIcon } from '../components/icons';

export default function Favoritos() {
  const { user } = useAuth();
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!user) return;
    request('/favorites')
      .then(setCars)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [user]);

  async function remove(car) {
    try {
      await request(`/favorites/${car.id}`, { method: 'DELETE' });
      setCars((prev) => prev.filter((c) => c.id !== car.id));
    } catch (e) {
      setError(e.message);
    }
  }

  if (!user) {
    return (
      <section>
        <p className="eyebrow">Favoritos</p>
        <h1>Tus coches guardados</h1>
        <div className="card empty-card">
          <span className="icon-tile"><HeartIcon /></span>
          <p>Para guardar coches necesitas una cuenta.</p>
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
      <p className="eyebrow">Favoritos</p>
      <h1>Tus coches guardados</h1>
      {error && <p className="alert">{error}</p>}
      {loading && <p className="muted">Cargando favoritos…</p>}
      {!loading && cars.length === 0 && (
        <div className="card empty-card">
          <span className="icon-tile"><HeartIcon /></span>
          <p>
            Todavía no has guardado ningún coche. Entra en una ficha y pulsa
            «Guardar en favoritos».
          </p>
          <div className="detail-actions">
            <Link className="btn btn-primary" to="/catalogo">Ver catálogo</Link>
          </div>
        </div>
      )}
      <div className="grid">
        {cars.map((car) => (
          <CarCard key={car.id} car={car} onRemove={remove} />
        ))}
      </div>
    </section>
  );
}
