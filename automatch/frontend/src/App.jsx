import { Routes, Route, Link } from 'react-router-dom';
import Navbar from './components/Navbar';
import RequireAdmin from './components/RequireAdmin';
import ScrollTopButton from './components/ScrollTopButton';
import Home from './pages/Home';
import Catalogo from './pages/Catalogo';
import CarDetail from './pages/CarDetail';
import Comparador from './pages/Comparador';
import Recomendador from './pages/Recomendador';
import Tco from './pages/Tco';
import Favoritos from './pages/Favoritos';
import Login from './pages/Login';
import Registro from './pages/Registro';
import VenderCoche from './pages/VenderCoche';
import MisAnuncios from './pages/MisAnuncios';
import EditarAnuncio from './pages/EditarAnuncio';
import AdminPanel from './pages/AdminPanel';
import AdminCoches from './pages/AdminCoches';
import AdminModeracion from './pages/AdminModeracion';
import AdminUsuarios from './pages/AdminUsuarios';

export default function App() {
  return (
    <>
      <Navbar />
      <main className="container">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/catalogo" element={<Catalogo />} />
          <Route path="/coches/:id" element={<CarDetail />} />
          <Route path="/comparador" element={<Comparador />} />
          <Route path="/recomendador" element={<Recomendador />} />
          <Route path="/tco" element={<Tco />} />
          <Route path="/favoritos" element={<Favoritos />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/vender" element={<VenderCoche />} />
          <Route path="/mis-anuncios" element={<MisAnuncios />} />
          <Route path="/mis-anuncios/:id/editar" element={<EditarAnuncio />} />
          <Route path="/admin" element={<RequireAdmin><AdminPanel /></RequireAdmin>} />
          <Route path="/admin/coches" element={<RequireAdmin><AdminCoches /></RequireAdmin>} />
          <Route path="/admin/moderacion" element={<RequireAdmin><AdminModeracion /></RequireAdmin>} />
          <Route path="/admin/usuarios" element={<RequireAdmin><AdminUsuarios /></RequireAdmin>} />
        </Routes>
      </main>
      <footer className="footer">
        <div className="footer-inner">
          <span className="brand-plate" aria-hidden="true">
            <span className="brand-plate-band">E</span>
            <span className="brand-plate-text">AUTO</span>
          </span>
          <p>AutoMatch · TFG Ingeniería Informática · Datos y precios orientativos</p>
          <nav className="footer-links">
            <Link to="/catalogo">Catálogo</Link>
            <Link to="/recomendador">Recomendador</Link>
            <Link to="/tco">Coste real</Link>
          </nav>
        </div>
      </footer>
      <ScrollTopButton />
    </>
  );
}
