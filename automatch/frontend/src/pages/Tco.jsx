import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { request, formatEuro } from '../api';
import CopyLinkButton from '../components/CopyLinkButton';
import { ReceiptIcon } from '../components/icons';

const CATEGORY_LABELS = {
  depreciacion: 'Depreciación',
  energia: 'Combustible / energía',
  seguro: 'Seguro',
  mantenimiento: 'Mantenimiento',
  impuestos: 'Impuestos',
};

/** Número desde un query param, acotado al rango del slider; fallback si falta o no es válido. */
function numParam(value, min, max, fallback) {
  const n = Number(value);
  if (!value || Number.isNaN(n)) return fallback;
  return Math.min(max, Math.max(min, n));
}

export default function Tco() {
  const [searchParams, setSearchParams] = useSearchParams();

  const [cars, setCars] = useState([]);
  const [carId, setCarId] = useState(searchParams.get('car') || '');
  const [years, setYears] = useState(() => numParam(searchParams.get('years'), 1, 10, 5));
  const [km, setKm] = useState(() => numParam(searchParams.get('km'), 5000, 40000, 15000));

  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  // URL compartible: coche, años y km viajan como query params
  useEffect(() => {
    if (!carId) {
      setSearchParams({}, { replace: true });
      return;
    }
    setSearchParams({ car: carId, years: String(years), km: String(km) }, { replace: true });
  }, [carId, years, km, setSearchParams]);

  useEffect(() => {
    request('/cars/all')
      .then(setCars)
      .catch((e) => setError(e.message));
  }, []);

  useEffect(() => {
    if (!carId) {
      setResult(null);
      return;
    }
    setError('');
    request(`/tco/${carId}?years=${years}&km=${km}`)
      .then(setResult)
      .catch((e) => setError(e.message));
  }, [carId, years, km]);

  const categories = result ? Object.entries(result.breakdown) : [];

  return (
    <section>
      <div className="page-hero">
        <span className="page-hero-icon"><ReceiptIcon /></span>
        <div className="page-hero-body">
          <p className="eyebrow">Coste total de propiedad</p>
          <h1>Lo que de verdad cuesta un coche</h1>
          <p className="lead">
            El precio de compra es solo una parte: la depreciación, la energía, el
            seguro, el mantenimiento y los impuestos deciden el resto.
          </p>
        </div>
      </div>

      <div className="card tco-controls">
        <label className="field">
          Coche
          <select value={carId} onChange={(e) => setCarId(e.target.value)}>
            <option value="">Elige un coche…</option>
            {cars.map((car) => (
              <option key={car.id} value={car.id}>
                {car.brand} {car.model} · {formatEuro(car.price)}
              </option>
            ))}
          </select>
        </label>

        <label className="field">
          Años de uso: <strong>{years}</strong>
          <input
            type="range"
            min="1"
            max="10"
            step="1"
            value={years}
            onChange={(e) => setYears(Number(e.target.value))}
          />
        </label>

        <label className="field">
          Kilómetros al año: <strong>{km.toLocaleString('es-ES')}</strong>
          <input
            type="range"
            min="5000"
            max="40000"
            step="1000"
            value={km}
            onChange={(e) => setKm(Number(e.target.value))}
          />
        </label>
      </div>

      {error && <p className="alert">{error}</p>}

      {!carId && !error && (
        <div className="card empty-card">
          <span className="icon-tile"><ReceiptIcon /></span>
          <p>
            Elige un coche arriba para ver el desglose completo de su coste
            real: depreciación, energía, seguro, mantenimiento e impuestos.
          </p>
        </div>
      )}

      {result && (
        <div className="card tco-result">
          <div className="results-bar">
            <div>
              <h2>{result.carName}</h2>
              <p className="muted">
                {result.years} años · {result.kmPerYear.toLocaleString('es-ES')} km/año
              </p>
            </div>
            <CopyLinkButton />
          </div>

          <div className="tco-bars">
            {categories.map(([key, value], index) => (
              <div key={key} className="tco-row">
                <span className="tco-label">{CATEGORY_LABELS[key] || key}</span>
                <div className="tco-track">
                  <div
                    className={`tco-fill tco-cat-${index}`}
                    style={{ width: `${(value / result.totalCost) * 100}%` }}
                  />
                </div>
                <span className="tco-value">{formatEuro(value)}</span>
              </div>
            ))}
          </div>

          <div className="tco-totals">
            <div>
              <span>Total {result.years} años</span>
              <strong>{formatEuro(result.totalCost)}</strong>
            </div>
            <div>
              <span>Al año</span>
              <strong>{formatEuro(result.costPerYear)}</strong>
            </div>
            <div>
              <span>Al mes</span>
              <strong>{formatEuro(result.totalCost / result.years / 12)}</strong>
            </div>
            <div>
              <span>Por km</span>
              <strong>
                {result.costPerKm.toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 3 })} €
              </strong>
            </div>
          </div>

          <p className="hint muted">
            Estimación con precios medios de carburante y electricidad en España;
            puedes ajustar las constantes en el backend (CarCosts.java).
          </p>
        </div>
      )}
    </section>
  );
}
