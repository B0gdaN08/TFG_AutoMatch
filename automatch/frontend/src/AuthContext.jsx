import { createContext, useContext, useEffect, useState } from 'react';
import { request, setUnauthorizedHandler } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('automatch_user');
    return saved ? JSON.parse(saved) : null;
  });

  function saveSession(data) {
    localStorage.setItem('automatch_token', data.token);
    const info = { email: data.email, name: data.name, role: data.role };
    localStorage.setItem('automatch_user', JSON.stringify(info));
    setUser(info);
  }

  async function login(email, password) {
    const data = await request('/auth/login', { method: 'POST', body: { email, password } });
    saveSession(data);
  }

  async function register(name, email, password) {
    const data = await request('/auth/register', { method: 'POST', body: { name, email, password } });
    saveSession(data);
  }

  function logout() {
    localStorage.removeItem('automatch_token');
    localStorage.removeItem('automatch_user');
    setUser(null);
  }

  // Si una petición vuelve con 401 (token caducado o inválido), cerramos la sesión local.
  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, []);

  const isAdmin = user?.role === 'ROLE_ADMIN';

  return (
    <AuthContext.Provider value={{ user, isAdmin, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
