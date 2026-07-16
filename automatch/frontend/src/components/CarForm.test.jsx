import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CarForm from './CarForm';
import { request } from '../api';

vi.mock('../api', async (importOriginal) => {
  const actual = await importOriginal();
  return { ...actual, request: vi.fn() };
});

const CATALOGO = [
  { brand: 'Dacia', models: ['Sandero', 'Spring'] },
  { brand: 'Toyota', models: ['Corolla 140H', 'Yaris Hybrid'] },
];

describe('CarForm en modo vendedor', () => {
  beforeEach(() => {
    request.mockReset();
    request.mockResolvedValue(CATALOGO);
  });

  it('muestra desplegables de marca y modelo alimentados por la base de conocimiento', async () => {
    render(<CarForm sellerMode onSubmit={() => {}} />);

    // Mientras carga el catálogo se muestra un input libre; esperar al desplegable
    const marca = await waitFor(() => {
      const el = screen.getByLabelText(/Marca/);
      expect(el.tagName).toBe('SELECT');
      return el;
    });
    expect([...marca.options].map((o) => o.text)).toContain('Toyota');
    expect([...marca.options].map((o) => o.text)).toContain('Otra marca…');

    // El modelo está deshabilitado hasta elegir marca
    const modelo = screen.getByLabelText(/Modelo/);
    expect(modelo).toBeDisabled();

    // Al elegir Toyota, el desplegable de modelo se llena con sus modelos
    fireEvent.change(marca, { target: { value: 'Toyota' } });
    const opcionesModelo = [...screen.getByLabelText(/Modelo/).options].map((o) => o.text);
    expect(opcionesModelo).toContain('Corolla 140H');
    expect(opcionesModelo).toContain('Yaris Hybrid');
    expect(opcionesModelo).not.toContain('Sandero');
    expect(opcionesModelo).toContain('Otro modelo…');
  });

  it('oculta los campos expertos y muestra el aviso de cálculo automático', async () => {
    render(<CarForm sellerMode onSubmit={() => {}} />);
    await waitFor(() => expect(screen.getByLabelText(/Marca/).tagName).toBe('SELECT'));

    expect(screen.queryByLabelText(/Fiabilidad/)).toBeNull();
    expect(screen.queryByLabelText(/Seguridad/)).toBeNull();
    expect(screen.getByText(/los calcula AutoMatch automáticamente/)).toBeInTheDocument();
  });

  it('en modo admin mantiene los campos libres y las puntuaciones editables', () => {
    render(<CarForm onSubmit={() => {}} />);

    expect(screen.getByLabelText(/Marca/).tagName).toBe('INPUT');
    expect(screen.getByLabelText(/Fiabilidad/)).toBeInTheDocument();
    expect(request).not.toHaveBeenCalled();
  });
});
