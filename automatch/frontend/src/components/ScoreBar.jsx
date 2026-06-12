/**
 * Barra horizontal de puntuación.
 * value: valor actual · max: valor máximo · tone: 'amber' | 'blue' (relleno)
 */
export default function ScoreBar({ value, max = 100, tone = 'amber', label }) {
  const pct = Math.max(0, Math.min(100, (value / max) * 100));
  return (
    <div className="scorebar" role="img" aria-label={label || `${value} de ${max}`}>
      <div className={`scorebar-fill scorebar-${tone}`} style={{ width: `${pct}%` }} />
    </div>
  );
}
