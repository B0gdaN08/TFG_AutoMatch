import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { request } from '../api';
import { useAuth } from '../AuthContext';
import CarForm from '../components/CarForm';
import { TagIcon } from '../components/icons';

export default function VenderCoche() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(payload) {
    setError('');
    setLoading(true);
    try {
      await request('/cars', { method: 'POST', body: payload });
      navigate('/mis-anuncios', { state: { justPublished: true } });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (!user) {
    return (
      <section>
        <p className="eyebrow">Vender coche</p>
        <h1>Publica tu coche en venta</h1>
        <div className="card empty-card">
          <span className="icon-tile"><TagIcon /></span>
          <p>Para publicar un anuncio necesitas una cuenta.</p>
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
      <p className="eyebrow">Vender coche</p>
      <h1>Publica tu coche en venta</h1>
      <p className="lead">
        Rellena la ficha técnica con los mismos datos que el catálogo, para que
        cualquier comprador pueda comparar tu coche con cualquier otro. Tu
        anuncio quedará pendiente de revisión por un administrador antes de
        aparecer en el catálogo.
      </p>
      {error && <p className="alert">{error}</p>}
      <div className="card">
        <CarForm onSubmit={handleSubmit} submitLabel="Publicar anuncio" loading={loading} sellerMode />
      </div>
    </section>
  );
}
