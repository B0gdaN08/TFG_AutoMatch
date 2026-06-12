import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { request } from '../api';
import CountUp from '../components/CountUp';
import Reveal from '../components/Reveal';
import { GridIcon, ClipboardCheckIcon, UsersIcon } from '../components/icons';

export default function AdminPanel() {
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    request('/admin/stats').then(setStats).catch((e) => setError(e.message));
  }, []);

  return (
    <section>
      <p className="eyebrow">Administración</p>
      <h1>Panel de administración</h1>
      {error && <p className="alert">{error}</p>}

      {stats && (
        <div className="stats-grid">
          <Reveal as="div" className="card stat-card" delay={0}>
            <span className="stat-value"><CountUp value={stats.catalogCars} /></span>
            <span className="stat-label">Coches del catálogo</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={40}>
            <span className="stat-value"><CountUp value={stats.userListings} /></span>
            <span className="stat-label">Anuncios de usuarios</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={80}>
            <span className="stat-value"><CountUp value={stats.pendingListings} /></span>
            <span className="stat-label">Pendientes de revisión</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={120}>
            <span className="stat-value"><CountUp value={stats.approvedListings} /></span>
            <span className="stat-label">Anuncios publicados</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={160}>
            <span className="stat-value"><CountUp value={stats.rejectedListings} /></span>
            <span className="stat-label">Anuncios rechazados</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={200}>
            <span className="stat-value"><CountUp value={stats.totalUsers} /></span>
            <span className="stat-label">Usuarios registrados</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={240}>
            <span className="stat-value"><CountUp value={stats.totalAdmins} /></span>
            <span className="stat-label">Administradores</span>
          </Reveal>
          <Reveal as="div" className="card stat-card" delay={280}>
            <span className="stat-value"><CountUp value={stats.totalFavorites} /></span>
            <span className="stat-label">Favoritos guardados</span>
          </Reveal>
        </div>
      )}

      <div className="admin-links">
        <Link className="card admin-link" to="/admin/coches">
          <span className="icon-tile"><GridIcon /></span>
          <h2>Coches del catálogo</h2>
          <p className="muted">Añade, edita o elimina cualquier coche del catálogo oficial.</p>
          <span className="tool-go">Gestionar <span aria-hidden="true">→</span></span>
        </Link>
        <Link className="card admin-link" to="/admin/moderacion">
          <span className="icon-tile"><ClipboardCheckIcon /></span>
          <h2>Moderación de anuncios{stats?.pendingListings ? ` (${stats.pendingListings})` : ''}</h2>
          <p className="muted">Revisa y aprueba o rechaza los anuncios publicados por usuarios.</p>
          <span className="tool-go">Revisar <span aria-hidden="true">→</span></span>
        </Link>
        <Link className="card admin-link" to="/admin/usuarios">
          <span className="icon-tile"><UsersIcon /></span>
          <h2>Usuarios</h2>
          <p className="muted">Gestiona los roles y cuentas de los usuarios registrados.</p>
          <span className="tool-go">Gestionar <span aria-hidden="true">→</span></span>
        </Link>
      </div>
    </section>
  );
}
