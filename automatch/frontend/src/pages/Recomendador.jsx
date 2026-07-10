import { useState } from 'react';
import { Link } from 'react-router-dom';
import { request, FUEL_LABELS, TYPE_LABELS, formatEuro } from '../api';
import Plate from '../components/Plate';
import ScoreBar from '../components/ScoreBar';
import { TargetIcon } from '../components/icons';

const CRITERIA = [
  { key: 'price', label: 'Precio bajo' },
  { key: 'efficiency', label: 'Coste de uso bajo' },
  { key: 'reliability', label: 'Fiabilidad' },
  { key: 'safety', label: 'Seguridad' },
  { key: 'space', label: 'Espacio' },
  { key: 'performance', label: 'Prestaciones' },
  { key: 'equipment', label: 'Equipamiento' },
];

export default function Recomendador() {
  const [weights, setWeights] = useState({
    price: 4,
    efficiency: 3,
    reliability: 4,
    safety: 3,
    space: 2,
    performance: 2,
    equipment: 2,
  });
  const [maxPrice, setMaxPrice] = useState('');
  const [type, setType] = useState('');
  const [fuels, setFuels] = useState([]);
  const [minSeats, setMinSeats] = useState('');

  const [method, setMethod] = useState('WEIGHTED_SUM');
  const [usedMethod, setUsedMethod] = useState(null);

  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function setWeight(key, value) {
    setWeights((prev) => ({ ...prev, [key]: Number(value) }));
  }

  function toggleFuel(fuel) {
    setFuels((prev) =>
      prev.includes(fuel) ? prev.filter((f) => f !== fuel) : [...prev, fuel]
    );
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      const data = await request('/recommendations', {
        method: 'POST',
        body: {
          maxPrice: maxPrice ? Number(maxPrice) : null,
          types: type ? [type] : [],
          fuels,
          minSeats: minSeats ? Number(minSeats) : null,
          weights,
          method,
        },
      });
      setResults(data);
      setUsedMethod(method);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <section>
      <div className="page-hero">
        <span className="page-hero-icon"><TargetIcon /></span>
        <div className="page-hero-body">
          <p className="eyebrow">Recomendador multicriterio</p>
          <h1>Dinos qué te importa</h1>
          <p className="lead">
            Ajusta la importancia de cada criterio (0 = me da igual, 5 = clave) y
            calcula tu ranking personalizado.
          </p>
        </div>
      </div>

      <div className="reco-layout">
        <form className="card reco-form" onSubmit={handleSubmit}>
          <h2>Tus prioridades</h2>
          {CRITERIA.map((criterion) => (
            <label key={criterion.key} className="slider-row">
              <span className="slider-label">{criterion.label}</span>
              <input
                type="range"
                min="0"
                max="5"
                step="1"
                value={weights[criterion.key]}
                onChange={(e) => setWeight(criterion.key, e.target.value)}
              />
              <span className="slider-value">{weights[criterion.key]}</span>
            </label>
          ))}

          <h2>Algoritmo</h2>
          <label className="field">
            Método de cálculo
            <select value={method} onChange={(e) => setMethod(e.target.value)}>
              <option value="WEIGHTED_SUM">Suma Ponderada — media ponderada de criterios normalizados</option>
              <option value="TOPSIS">TOPSIS — proximidad a la solución ideal (Hwang y Yoon, 1981)</option>
            </select>
          </label>

          <h2>Filtros</h2>
          <label className="field">
            Presupuesto máximo (€)
            <input
              type="number"
              min="0"
              step="1000"
              placeholder="Sin límite"
              value={maxPrice}
              onChange={(e) => setMaxPrice(e.target.value)}
            />
          </label>

          <label className="field">
            Tipo de coche
            <select value={type} onChange={(e) => setType(e.target.value)}>
              <option value="">Cualquiera</option>
              {Object.entries(TYPE_LABELS).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </label>

          <fieldset className="field">
            <legend>Combustible (vacío = todos)</legend>
            {Object.entries(FUEL_LABELS).map(([value, label]) => (
              <label key={value} className="check-row">
                <input
                  type="checkbox"
                  checked={fuels.includes(value)}
                  onChange={() => toggleFuel(value)}
                />
                {label}
              </label>
            ))}
          </fieldset>

          <label className="field">
            Plazas mínimas
            <select value={minSeats} onChange={(e) => setMinSeats(e.target.value)}>
              <option value="">Indiferente</option>
              <option value="5">5</option>
              <option value="7">7</option>
            </select>
          </label>

          <button className="btn btn-primary btn-block" disabled={loading}>
            {loading ? 'Calculando…' : 'Calcular mi ranking'}
          </button>
        </form>

        <div className="reco-results">
          {error && <p className="alert">{error}</p>}

          {results === null && !error && (
            <div className="card empty-card">
              <span className="icon-tile"><TargetIcon /></span>
              <p>
                Aquí aparecerá tu top de coches con la puntuación de cada uno y
                el detalle por criterio.
              </p>
            </div>
          )}

          {results && results.length === 0 && (
            <div className="card empty-card">
              <span className="icon-tile"><TargetIcon /></span>
              <p>Ningún coche cumple esos filtros. Prueba a relajarlos.</p>
            </div>
          )}

          {results && results.length > 0 && (
            <p className="muted" style={{ marginBottom: '0.5rem' }}>
              Método usado:{' '}
              <strong>{usedMethod === 'TOPSIS' ? 'TOPSIS (Hwang y Yoon)' : 'Suma Ponderada'}</strong>
            </p>
          )}

          {results && results.map((result, index) => (
            <article key={result.car.id} className="card reco-card">
              <div className="reco-rank">{index + 1}</div>
              <div className="reco-body">
                <div className="reco-head">
                  <Plate text={result.car.brand} />
                  <div>
                    <h3>{result.car.brand} {result.car.model}</h3>
                    <p className="car-card-sub">
                      {TYPE_LABELS[result.car.type]} · {FUEL_LABELS[result.car.fuel]} ·{' '}
                      {formatEuro(result.car.price)}
                    </p>
                  </div>
                  <div className="reco-score">
                    <strong>{result.score.toLocaleString('es-ES')}</strong>
                    <span>/100</span>
                  </div>
                </div>

                <ScoreBar value={result.score} max={100} tone="amber" label={`Puntuación ${result.score} de 100`} />

                <div className="criteria-grid">
                  {CRITERIA.map((criterion) => (
                    <div
                      key={criterion.key}
                      className="criteria-item"
                      title={`${criterion.label}: ${Math.round(result.criteriaScores[criterion.key] * 100)}/100`}
                    >
                      <span>{criterion.label}</span>
                      <ScoreBar
                        value={result.criteriaScores[criterion.key]}
                        max={1}
                        tone="blue"
                        label={criterion.label}
                      />
                    </div>
                  ))}
                </div>

                <Link className="btn btn-ghost btn-sm" to={`/coches/${result.car.id}`}>
                  Ver ficha completa
                </Link>
              </div>
            </article>
          ))}
        </div>
      </div>
    </section>
  );
}
