import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import Plate from './Plate';

describe('Plate', () => {
  it('recorta el texto a 3 letras y lo pone en mayúsculas', () => {
    const { container } = render(<Plate text="volkswagen" />);
    expect(container.querySelector('.plate-text')).toHaveTextContent('VOL');
  });

  it('elimina los caracteres que no son letras antes de recortar', () => {
    const { container } = render(<Plate text="3M Motors" />);
    expect(container.querySelector('.plate-text')).toHaveTextContent('MMO');
  });
});
