import { useEffect, useState } from 'react';
import { request } from '../api';
import { useAuth } from '../AuthContext';
import ConfirmDialog from '../components/ConfirmDialog';

export default function AdminUsuarios() {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [pendingRemove, setPendingRemove] = useState(null);

  function load() {
    setLoading(true);
    request('/admin/users')
      .then(setUsers)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }

  useEffect(load, []);

  async function toggleRole(target) {
    const newRole = target.role === 'ROLE_ADMIN' ? 'ROLE_USER' : 'ROLE_ADMIN';
    try {
      const updated = await request(`/admin/users/${target.id}/role`, {
        method: 'PUT',
        body: { role: newRole },
      });
      setUsers((prev) => prev.map((u) => (u.id === target.id ? updated : u)));
    } catch (e) {
      setError(e.message);
    }
  }

  async function remove(target) {
    try {
      await request(`/admin/users/${target.id}`, { method: 'DELETE' });
      setUsers((prev) => prev.filter((u) => u.id !== target.id));
    } catch (e) {
      setError(e.message);
    } finally {
      setPendingRemove(null);
    }
  }

  return (
    <section>
      <p className="eyebrow">Administración</p>
      <h1>Usuarios</h1>
      {error && <p className="alert">{error}</p>}
      {loading && <p className="muted">Cargando usuarios…</p>}

      {!loading && (
        <div className="card table-wrap admin-table-wrap">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Email</th>
                <th>Rol</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => {
                const isSelf = u.email === user.email;
                return (
                  <tr key={u.id}>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.role === 'ROLE_ADMIN' ? 'Administrador' : 'Usuario'}</td>
                    <td className="admin-table-actions">
                      <button className="btn btn-ghost btn-sm" disabled={isSelf} onClick={() => toggleRole(u)}>
                        {u.role === 'ROLE_ADMIN' ? 'Quitar admin' : 'Hacer admin'}
                      </button>
                      <button className="btn btn-ghost btn-sm" disabled={isSelf} onClick={() => setPendingRemove(u)}>
                        Borrar
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
      <ConfirmDialog
        open={!!pendingRemove}
        title="Borrar usuario"
        message={pendingRemove ? `¿Seguro que quieres borrar la cuenta de ${pendingRemove.email}? También se eliminarán sus anuncios. Esta acción no se puede deshacer.` : ''}
        confirmLabel="Borrar cuenta"
        danger
        onConfirm={() => remove(pendingRemove)}
        onCancel={() => setPendingRemove(null)}
      />
    </section>
  );
}
