import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { request } from '../api';
import { useAuth } from '../AuthContext';
import CarForm from '../components/CarForm';
import { TagIcon } from '../components/icons';

export default function EditarAnuncio() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();

  const [car, setCar] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user) return;
    request(`/cars/${id}`)
      .then((data) => {
        if (!data.own) {
          setError('Solo puedes editar tus propios anuncios.');
        } else {
          setCar(data);
        }
      })
      .catch((e) => setError(e.message));
  }, [id, user]);

  async function handleSubmit(payload) {
    setError('');
    setLoading(true);
    try {
      await request(`/cars/${id}`, { method: 'PUT', body: payload });
      navigate('/mis-anuncios', { state: { justEdited: true } });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  if (!user) {
    return (
      <section>
        <p className="eyebrow">Editar anuncio</p>
        <h1>Edita tu coche en venta</h1>
        <div className="card empty-card">
          <span className="icon-tile"><TagIcon /></span>
          <p>Para editar un anuncio necesitas una cuenta.</p>
          <div className="detail-actions">
            <Link className="btn btn-primary" to="/login">Entrar</Link>
          </div>
        </div>
      </section>
    );
  }

  return (
    <section>
      <Link to="/mis-anuncios" className="backlink">← Volver a mis anuncios</Link>
      <p className="eyebrow">Editar anuncio</p>
      <h1>Edita tu coche en venta</h1>
      <p className="alert alert-info">
        Al guardar los cambios, tu anuncio volverá a quedar pendiente de revisión
        por un administrador antes de aparecer de nuevo en el catálogo.
      </p>
      {error && <p className="alert">{error}</p>}
      {!car && !error && <p className="muted">Cargando el anuncio…</p>}
      {car && (
        <div className="card">
          <CarForm
            initialValue={car}
            onSubmit={handleSubmit}
            submitLabel="Guardar cambios"
            loading={loading}
          />
        </div>
      )}
    </section>
  );
}
