import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <section className="notfound">
      {/* Matrícula con el código de error (el componente Plate solo admite letras) */}
      <span className="plate" aria-hidden="true">
        <span className="plate-band">E</span>
        <span className="plate-text">404</span>
      </span>
      <h1>Esta salida no existe</h1>
      <p className="muted">
        La página que buscas no está en el mapa. Vuelve a la vía principal.
      </p>
      <div className="detail-actions" style={{ justifyContent: 'center' }}>
        <Link className="btn btn-primary" to="/">Ir al inicio</Link>
        <Link className="btn btn-ghost" to="/catalogo">Ver el catálogo</Link>
      </div>
    </section>
  );
}
