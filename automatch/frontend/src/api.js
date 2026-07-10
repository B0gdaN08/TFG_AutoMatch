// URL del backend configurable en build (VITE_API_URL). Si no se define se usa
// el backend local de desarrollo. Una cadena vacía significa "rutas relativas"
// (en Docker, nginx hace de proxy de /api, /uploads e /images al backend).
const ORIGIN = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';
const API = `${ORIGIN}/api`;

export const MAX_IMAGES = 6;

export function getToken() {
  return localStorage.getItem('automatch_token');
}

/** Avisa a AuthContext cuando una petición devuelve 401 (sesión caducada o inválida). */
let unauthorizedHandler = null;
export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn;
}

/** Convierte una ruta relativa ("/uploads/xxx.jpg") en una URL completa al backend. */
export function imageUrl(path) {
  if (!path) return null;
  return path.startsWith('http') ? path : `${ORIGIN}${path}`;
}

/** Sube una foto y devuelve { url } con la ruta donde quedó guardada. */
export async function uploadImage(file) {
  const headers = {};
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${API}/cars/images`, { method: 'POST', headers, body: formData });

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    throw new Error(data?.message || `Error ${res.status}`);
  }
  return data;
}

/**
 * Pequeño cliente HTTP: añade el token JWT si existe y
 * convierte los errores del backend en excepciones con mensaje.
 */
export async function request(path, { method = 'GET', body } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(`${API}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);
  if (!res.ok) {
    if (res.status === 401) {
      unauthorizedHandler?.();
    }
    throw new Error(data?.message || `Error ${res.status}`);
  }
  return data;
}

export const FUEL_LABELS = {
  GASOLINA: 'Gasolina',
  DIESEL: 'Diésel',
  HIBRIDO: 'Híbrido',
  HIBRIDO_ENCHUFABLE: 'Híbrido enchufable',
  ELECTRICO: 'Eléctrico',
};

export const TYPE_LABELS = {
  URBANO: 'Urbano',
  COMPACTO: 'Compacto',
  BERLINA: 'Berlina',
  SUV: 'SUV',
  FAMILIAR: 'Familiar',
};

export const STATUS_LABELS = {
  PENDING: 'Pendiente de revisión',
  APPROVED: 'Publicado',
  REJECTED: 'Rechazado',
};

export function formatEuro(value) {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    maximumFractionDigits: 0,
  }).format(value);
}
