import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { request, TYPE_LABELS, STATUS_LABELS, formatEuro } from '../api';
import CarForm from '../components/CarForm';
import ConfirmDialog from '../components/ConfirmDialog';

export default function AdminCoches() {
  const [cars, setCars] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(null); // null = cerrado, 'new' = crear, coche = editar
  const [saving, setSaving] = useState(false);
  const [pendingRemove, setPendingRemove] = useState(null);

  function load() {
    setLoading(true);
    request('/admin/cars')
      .then(setCars)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function handleSubmit(payload) {
    setSaving(true);
    setError('');
    try {
      if (editing === 'new') {
        await request('/cars/catalog', { method: 'POST', body: payload });
      } else {
        await request(`/cars/${editing.id}`, { method: 'PUT', body: payload });
      }
      setEditing(null);
      load();
    } catch (e) {
      setError(e.message);
    } finally {
      setSaving(false);
    }
  }

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

  return (
    <section>
      <p className="eyebrow">Administración</p>
      <h1>Coches</h1>
      {error && <p className="alert">{error}</p>}

      {editing ? (
        <div className="card">
          <h2>{editing === 'new' ? 'Añadir coche al catálogo' : `Editar ${editing.brand} ${editing.model}`}</h2>
          <CarForm
            initialValue={editing === 'new' ? null : editing}
            onSubmit={handleSubmit}
            submitLabel={editing === 'new' ? 'Añadir al catálogo' : 'Guardar cambios'}
            loading={saving}
          />
          <button className="btn btn-ghost" onClick={() => setEditing(null)}>Cancelar</button>
        </div>
      ) : (
        <button className="btn btn-primary" onClick={() => setEditing('new')}>Añadir coche al catálogo</button>
      )}

      {loading && <p className="muted">Cargando coches…</p>}

      {!loading && (
        <div className="card table-wrap admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Coche</th>
                <th>Tipo</th>
                <th>Precio</th>
                <th>Estado</th>
                <th>Propietario</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {cars.map((car) => (
                <tr key={car.id}>
                  <td><Link to={`/coches/${car.id}`}>{car.brand} {car.model}</Link></td>
                  <td>{TYPE_LABELS[car.type]}</td>
                  <td>{formatEuro(car.price)}</td>
                  <td>
                    <span className={`chip chip-status-${car.status.toLowerCase()}`}>
                      {STATUS_LABELS[car.status]}
                    </span>
                  </td>
                  <td>{car.ownerId ? `Usuario #${car.ownerId}` : 'Catálogo'}</td>
                  <td className="admin-table-actions">
                    <button className="btn btn-ghost btn-sm" onClick={() => setEditing(car)}>Editar</button>
                    <button className="btn btn-ghost btn-sm" onClick={() => setPendingRemove(car)}>Borrar</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      <ConfirmDialog
        open={!!pendingRemove}
        title="Borrar coche"
        message={pendingRemove ? `¿Seguro que quieres borrar ${pendingRemove.brand} ${pendingRemove.model} del catálogo? Esta acción no se puede deshacer.` : ''}
        confirmLabel="Borrar"
        danger
        onConfirm={() => remove(pendingRemove)}
        onCancel={() => setPendingRemove(null)}
      />
    </section>
  );
}
