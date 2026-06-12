import { useEffect, useState } from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    function onScroll() {
      setScrolled(window.scrollY > 8);
    }
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  function handleLogout() {
    logout();
    navigate('/');
  }

  return (
    <header className={`navbar ${scrolled ? 'is-scrolled' : ''}`}>
      <Link to="/" className="brand">
        <span className="brand-plate">
          <span className="brand-plate-band">E</span>
          <span className="brand-plate-text">AUTO</span>
        </span>
        <span className="brand-name">AutoMatch</span>
      </Link>

      <nav className="nav-links">
        <NavLink to="/" end>Inicio</NavLink>
        <NavLink to="/catalogo">Catálogo</NavLink>
        <NavLink to="/recomendador">Recomendador</NavLink>
        <NavLink to="/comparador">Comparador</NavLink>
        <NavLink to="/tco">Coste real</NavLink>
        <NavLink to="/favoritos">Favoritos</NavLink>
        {user && <NavLink to="/vender">Vender coche</NavLink>}
        {user && <NavLink to="/mis-anuncios">Mis anuncios</NavLink>}
        {isAdmin && <NavLink to="/admin">Admin</NavLink>}
      </nav>

      <div className="nav-auth">
        {user ? (
          <>
            <span className="nav-user" title={user.email}>Hola, {user.name}</span>
            <button className="btn btn-ghost" onClick={handleLogout}>Salir</button>
          </>
        ) : (
          <>
            <Link className="btn btn-ghost" to="/login">Entrar</Link>
            <Link className="btn btn-primary" to="/registro">Crear cuenta</Link>
          </>
        )}
      </div>
    </header>
  );
}
