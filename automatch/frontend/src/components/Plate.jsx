/** Distintivo estilo matrícula española con las primeras letras de la marca. */
export default function Plate({ text }) {
  const code = (text || '???').replace(/[^A-Za-zÁÉÍÓÚáéíóú]/g, '').slice(0, 3).toUpperCase();
  return (
    <span className="plate" aria-hidden="true">
      <span className="plate-band">E</span>
      <span className="plate-text">{code}</span>
    </span>
  );
}
