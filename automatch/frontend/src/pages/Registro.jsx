import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import AuthSide from '../components/AuthSide';

export default function Registro() {
  const { register } = useAuth();
  const navigate = useNavigate();
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await register(name, email, password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="auth-page">
      <AuthSide
        eyebrow="Únete en un minuto"
        title="Crea tu cuenta gratis"
        lead="Regístrate para guardar favoritos, publicar tu coche en venta y acceder a tus comparativas desde cualquier dispositivo."
      />
      <div className="auth-form-side">
        <div className="card auth-card">
          <p className="eyebrow">Tu cuenta</p>
          <h1>Crear cuenta</h1>
          <form onSubmit={handleSubmit} className="form">
            <label>
              Nombre
              <input value={name} onChange={(e) => setName(e.target.value)} required />
            </label>
            <label>
              Email
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </label>
            <label>
              Contraseña (mínimo 6 caracteres)
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required minLength={6} />
            </label>
            {error && <p className="alert">{error}</p>}
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Creando…' : 'Crear cuenta'}
            </button>
          </form>
          <p className="muted">
            ¿Ya tienes cuenta? <Link to="/login">Entra aquí</Link>.
          </p>
        </div>
      </div>
    </section>
  );
}
