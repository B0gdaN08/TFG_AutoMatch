import { describe, it, expect, beforeEach, vi } from 'vitest';
import { request, formatEuro } from './api';

describe('request', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('añade la cabecera Authorization cuando hay token en localStorage', async () => {
    localStorage.setItem('automatch_token', 'token-123');
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ ok: true }),
    });

    await request('/favorites');

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers.Authorization).toBe('Bearer token-123');
  });

  it('no añade la cabecera Authorization si no hay token guardado', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ ok: true }),
    });

    await request('/cars');

    const [, options] = global.fetch.mock.calls[0];
    expect(options.headers.Authorization).toBeUndefined();
  });

  it('convierte un error del backend {"message": "..."} en una excepción con ese mensaje', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      json: async () => ({ message: 'Credenciales incorrectas' }),
    });

    await expect(request('/auth/login', { method: 'POST', body: {} }))
      .rejects.toThrow('Credenciales incorrectas');
  });
});

describe('formatEuro', () => {
  it('formatea en es-ES con separador de miles "." y símbolo €, sin decimales', () => {
    expect(formatEuro(15000)).toBe('15.000 €');
    expect(formatEuro(20000.5)).toBe('20.001 €');
  });
});
