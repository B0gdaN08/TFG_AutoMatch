import { describe, it, expect, beforeEach } from 'vitest';
import { addToCompare, removeFromCompare, readCompareIds, MAX_COMPARE } from './compare';

describe('compare (selección persistente del comparador)', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('añade coches y los persiste en localStorage', () => {
    expect(addToCompare(1)).toBe(true);
    expect(addToCompare(2)).toBe(true);
    expect(readCompareIds()).toEqual([1, 2]);
    expect(JSON.parse(localStorage.getItem('automatch_compare'))).toEqual([1, 2]);
  });

  it('no duplica un coche ya seleccionado', () => {
    addToCompare(1);
    expect(addToCompare(1)).toBe(true);
    expect(readCompareIds()).toEqual([1]);
  });

  it(`rechaza el cuarto coche (máximo ${MAX_COMPARE})`, () => {
    addToCompare(1);
    addToCompare(2);
    addToCompare(3);
    expect(addToCompare(4)).toBe(false);
    expect(readCompareIds()).toEqual([1, 2, 3]);
  });

  it('quita un coche de la selección', () => {
    addToCompare(1);
    addToCompare(2);
    removeFromCompare(1);
    expect(readCompareIds()).toEqual([2]);
  });

  it('tolera contenido corrupto en localStorage', () => {
    localStorage.setItem('automatch_compare', 'esto-no-es-json{');
    expect(readCompareIds()).toEqual([]);

    localStorage.setItem('automatch_compare', '"tampoco-es-array"');
    expect(readCompareIds()).toEqual([]);
  });

  it('notifica los cambios mediante un evento propio', () => {
    let notificado = false;
    const listener = () => { notificado = true; };
    window.addEventListener('automatch:compare-change', listener);
    addToCompare(7);
    window.removeEventListener('automatch:compare-change', listener);
    expect(notificado).toBe(true);
  });
});
