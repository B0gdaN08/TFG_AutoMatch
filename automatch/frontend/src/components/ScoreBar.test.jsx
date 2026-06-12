import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import ScoreBar from './ScoreBar';

describe('ScoreBar', () => {
  it('renderiza el ancho proporcional correcto', () => {
    const { container } = render(<ScoreBar value={25} max={50} />);
    const fill = container.querySelector('.scorebar-fill');
    expect(fill.style.width).toBe('50%');
  });

  it('respeta el máximo cuando el valor lo supera', () => {
    const { container } = render(<ScoreBar value={150} max={100} />);
    const fill = container.querySelector('.scorebar-fill');
    expect(fill.style.width).toBe('100%');
  });

  it('no baja del 0% cuando el valor es negativo', () => {
    const { container } = render(<ScoreBar value={-10} max={100} />);
    const fill = container.querySelector('.scorebar-fill');
    expect(fill.style.width).toBe('0%');
  });
});
