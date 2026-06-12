import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext';
import AuthSide from '../components/AuthSide';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await login(email, password);
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
        eyebrow="Bienvenido de nuevo"
        title="Tu garaje, tal y como lo dejaste"
        lead="Entra para ver tus favoritos, tus anuncios publicados y retomar tus comparativas donde las dejaste."
      />
      <div className="auth-form-side">
        <div className="card auth-card">
          <p className="eyebrow">Tu cuenta</p>
          <h1>Entrar</h1>
          <form onSubmit={handleSubmit} className="form">
            <label>
              Email
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </label>
            <label>
              Contraseña
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
            </label>
            {error && <p className="alert">{error}</p>}
            <button className="btn btn-primary" disabled={loading}>
              {loading ? 'Entrando…' : 'Entrar'}
            </button>
          </form>
          <p className="muted">
            ¿Aún sin cuenta? <Link to="/registro">Créala en un minuto</Link>.
          </p>
          <p className="muted hint">
            Cuenta de prueba: demo@automatch.es · demo123
          </p>
        </div>
      </div>
    </section>
  );
}
