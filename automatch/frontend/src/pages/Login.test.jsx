import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Login from './Login';

const { mockLogin } = vi.hoisted(() => ({ mockLogin: vi.fn() }));

vi.mock('../AuthContext', () => ({
  useAuth: () => ({ login: mockLogin }),
}));

describe('Login', () => {
  beforeEach(() => {
    mockLogin.mockReset();
  });

  it('al enviar el formulario llama a login con email y contraseña', async () => {
    mockLogin.mockResolvedValue(undefined);
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'demo@automatch.es' } });
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'demo123' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    await waitFor(() => {
      expect(mockLogin).toHaveBeenCalledWith('demo@automatch.es', 'demo123');
    });
  });

  it('muestra el mensaje de error si login falla', async () => {
    mockLogin.mockRejectedValue(new Error('Credenciales incorrectas'));
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'demo@automatch.es' } });
    fireEvent.change(screen.getByLabelText('Contraseña'), { target: { value: 'malacontrasena' } });
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }));

    expect(await screen.findByText('Credenciales incorrectas')).toBeInTheDocument();
  });
});
