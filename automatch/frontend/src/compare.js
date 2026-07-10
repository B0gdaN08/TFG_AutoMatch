import { useEffect, useState } from 'react';

/**
 * Selección de coches para el comparador, persistida en localStorage.
 * Máximo 3 coches. Los cambios se propagan a todos los componentes montados
 * (badge de la navbar, botones de las tarjetas…) mediante un evento propio.
 */
const KEY = 'automatch_compare';
const EVENT = 'automatch:compare-change';

export const MAX_COMPARE = 3;

export function readCompareIds() {
  try {
    const ids = JSON.parse(localStorage.getItem(KEY));
    return Array.isArray(ids) ? ids.map(Number).filter(Boolean).slice(0, MAX_COMPARE) : [];
  } catch {
    return [];
  }
}

function write(ids) {
  localStorage.setItem(KEY, JSON.stringify(ids));
  window.dispatchEvent(new Event(EVENT));
}

/** Añade un coche a la selección. Devuelve false si ya hay 3 coches. */
export function addToCompare(id) {
  const ids = readCompareIds();
  if (ids.includes(id)) return true;
  if (ids.length >= MAX_COMPARE) return false;
  write([...ids, id]);
  return true;
}

export function removeFromCompare(id) {
  write(readCompareIds().filter((x) => x !== id));
}

/** Hook: lista de ids seleccionados, siempre sincronizada. */
export function useCompareIds() {
  const [ids, setIds] = useState(readCompareIds);

  useEffect(() => {
    const update = () => setIds(readCompareIds());
    window.addEventListener(EVENT, update);
    window.addEventListener('storage', update); // cambios desde otras pestañas
    return () => {
      window.removeEventListener(EVENT, update);
      window.removeEventListener('storage', update);
    };
  }, []);

  return ids;
}
