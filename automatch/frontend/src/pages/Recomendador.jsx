import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { request, FUEL_LABELS, TYPE_LABELS, formatEuro } from '../api';
import Plate from '../components/Plate';
import ScoreBar from '../components/ScoreBar';
import CopyLinkButton from '../components/CopyLinkButton';
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

const DEFAULT_WEIGHTS = {
  price: 4,
  efficiency: 3,
  reliability: 4,
  safety: 3,
  space: 2,
  performance: 2,
  equipment: 2,
};

/** Parsea el parámetro w=4,3,4,3,2,2,2 (orden de CRITERIA). Devuelve null si no es válido. */
function weightsFromParam(param) {
  if (!param) return null;
  const parts = param.split(',').map(Number);
  if (parts.length !== CRITERIA.length) return null;
  if (parts.some((n) => Number.isNaN(n) || n < 0 || n > 5)) return null;
  const weights = {};
  CRITERIA.forEach((criterion, i) => { weights[criterion.key] = parts[i]; });
  return weights;
}

export default function Recomendador() {
  const [searchParams, setSearchParams] = useSearchParams();

  // Estado inicial desde la URL: los enlaces compartidos reproducen la consulta
  const [weights, setWeights] = useState(
    () => weightsFromParam(searchParams.get('w')) || DEFAULT_WEIGHTS
  );
  const [maxPrice, setMaxPrice] = useState(() => searchParams.get('maxPrice') || '');
  const [type, setType] = useState(() => searchParams.get('type') || '');
  const [fuels, setFuels] = useState(
    () => (searchParams.get('fuels') || '').split(',').filter((f) => f in FUEL_LABELS)
  );
  const [minSeats, setMinSeats] = useState(() => searchParams.get('minSeats') || '');
  const [method, setMethod] = useState(
    () => (searchParams.get('method') === 'TOPSIS' ? 'TOPSIS' : 'WEIGHTED_SUM')
  );

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

  async function calculate(body) {
    setLoading(true);
    setError('');
    try {
      const data = await request('/recommendations', { method: 'POST', body });
      setResults(data);
      setUsedMethod(body.method);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function buildBody() {
    return {
      maxPrice: maxPrice ? Number(maxPrice) : null,
      types: type ? [type] : [],
      fuels,
      minSeats: minSeats ? Number(minSeats) : null,
      weights,
      method,
    };
  }

  // Enlace compartido: si la URL trae parámetros, se ejecuta el cálculo al entrar
  useEffect(() => {
    if (searchParams.get('w') || searchParams.get('method')) {
      calculate(buildBody());
    }
    // Solo al montar: el estado inicial ya viene de la URL
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function syncUrl() {
    const params = { w: CRITERIA.map((c) => weights[c.key]).join(',') };
    if (method !== 'WEIGHTED_SUM') params.method = method;
    if (maxPrice) params.maxPrice = maxPrice;
    if (type) params.type = type;
    if (fuels.length) params.fuels = fuels.join(',');
    if (minSeats) params.minSeats = minSeats;
    setSearchParams(params, { replace: true });
  }

  async function handleSubmit(e) {
    e.preventDefault();
    syncUrl();
    await calculate(buildBody());
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
            <div className="results-bar">
              <span className="muted">
                Método usado:{' '}
                <strong>{usedMethod === 'TOPSIS' ? 'TOPSIS (Hwang y Yoon)' : 'Suma Ponderada'}</strong>
              </span>
              <CopyLinkButton />
            </div>
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
