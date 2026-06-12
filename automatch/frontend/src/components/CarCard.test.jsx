import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import CarCard from './CarCard';
import { formatEuro, STATUS_LABELS } from '../api';

const baseCar = {
  id: 1,
  brand: 'Seat',
  model: 'Ibiza',
  year: 2022,
  price: 15000,
  type: 'COMPACTO',
  fuel: 'GASOLINA',
  dgtLabel: 'C',
  powerCv: 95,
  consumption: 5.5,
  trunkLiters: 355,
  seats: 5,
  status: 'APPROVED',
  imageUrls: [],
};

function renderCard(props = {}) {
  return render(
    <MemoryRouter>
      <CarCard car={{ ...baseCar, ...props.car }} {...props} />
    </MemoryRouter>
  );
}

describe('CarCard', () => {
  it('muestra marca, modelo, precio formateado y el chip de etiqueta DGT', () => {
    renderCard();

    expect(screen.getByText('Seat Ibiza')).toBeInTheDocument();
    // getByText normaliza espacios (incluido el NBSP de Intl.NumberFormat) a un espacio normal
    expect(screen.getByText(formatEuro(baseCar.price).replace(/\s/g, ' '))).toBeInTheDocument();
    expect(screen.getByText('C')).toHaveClass('chip-dgt-C');
  });

  it('no muestra el chip de estado cuando el coche está APPROVED', () => {
    renderCard({ car: { status: 'APPROVED' } });

    expect(document.querySelector('[class*="chip-status-"]')).toBeNull();
  });

  it('muestra el chip de estado cuando el coche no está APPROVED', () => {
    renderCard({ car: { status: 'PENDING' } });

    const chip = screen.getByText(STATUS_LABELS.PENDING);
    expect(chip).toBeInTheDocument();
    expect(chip).toHaveClass('chip-status-pending');
  });

  it('no muestra el botón de Quitar si no se pasa onRemove', () => {
    renderCard();

    expect(screen.queryByRole('button', { name: 'Quitar' })).not.toBeInTheDocument();
  });

  it('muestra el botón de Quitar si se pasa onRemove', () => {
    const onRemove = vi.fn();
    renderCard({ onRemove });

    expect(screen.getByRole('button', { name: 'Quitar' })).toBeInTheDocument();
  });
});
