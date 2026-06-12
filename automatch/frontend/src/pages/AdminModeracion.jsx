import { useEffect, useState } from 'react';
import { request } from '../api';
import CarCard from '../components/CarCard';

export default function AdminModeracion() {
  const [cars, setCars] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  function load() {
    setLoading(true);
    request('/cars/pending')
      .then(setCars)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function setStatus(car, action) {
    try {
      await request(`/cars/${car.id}/${action}`, { method: 'PUT' });
      setCars((prev) => prev.filter((c) => c.id !== car.id));
    } catch (e) {
      setError(e.message);
    }
  }

  return (
    <section>
      <p className="eyebrow">Administración</p>
      <h1>Moderación de anuncios</h1>
      {error && <p className="alert">{error}</p>}
      {loading && <p className="muted">Cargando anuncios pendientes…</p>}
      {!loading && cars.length === 0 && (
        <p className="muted">No hay anuncios pendientes de revisión.</p>
      )}
      <div className="grid">
        {cars.map((car) => (
          <div key={car.id} className="moderation-item">
            <CarCard car={car} />
            <div className="moderation-actions">
              <button className="btn btn-primary btn-sm" onClick={() => setStatus(car, 'approve')}>
                Aprobar
              </button>
              <button className="btn btn-ghost btn-sm" onClick={() => setStatus(car, 'reject')}>
                Rechazar
              </button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
