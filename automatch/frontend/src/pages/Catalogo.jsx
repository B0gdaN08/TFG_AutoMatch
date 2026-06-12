import { useEffect, useState } from 'react';
import { request, FUEL_LABELS, TYPE_LABELS } from '../api';
import CarCard from '../components/CarCard';
import { GridIcon, SearchIcon } from '../components/icons';

export default function Catalogo() {
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [search, setSearch] = useState('');
  const [type, setType] = useState('');
  const [fuel, setFuel] = useState('');
  const [maxPrice, setMaxPrice] = useState('');

  const hasFilters = Boolean(search || type || fuel || maxPrice);

  function clearFilters() {
    setSearch('');
    setType('');
    setFuel('');
    setMaxPrice('');
  }

  useEffect(() => {
    const params = new URLSearchParams();
    if (search) params.set('search', search);
    if (type) params.set('type', type);
    if (fuel) params.set('fuel', fuel);
    if (maxPrice) params.set('maxPrice', maxPrice);

    setLoading(true);
    setError('');
    request(`/cars?${params.toString()}`)
      .then(setCars)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [search, type, fuel, maxPrice]);

  return (
    <section>
      <div className="page-hero">
        <span className="page-hero-icon"><GridIcon /></span>
        <div className="page-hero-body">
          <p className="eyebrow">Catálogo · Mercado español</p>
          <h1>Catálogo</h1>
          <p className="lead">
            Cada coche con su ficha, sus puntuaciones y su coste real calculado.
            Filtra y quédate con los que encajan contigo.
          </p>
        </div>
      </div>

      <div className="filters card">
        <input
          type="search"
          placeholder="Buscar marca o modelo…"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="">Todos los tipos</option>
          {Object.entries(TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <select value={fuel} onChange={(e) => setFuel(e.target.value)}>
          <option value="">Todos los combustibles</option>
          {Object.entries(FUEL_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <input
          type="number"
          min="0"
          step="1000"
          placeholder="Precio máximo (€)"
          value={maxPrice}
          onChange={(e) => setMaxPrice(e.target.value)}
        />
      </div>

      {error && <p className="alert">{error}</p>}

      {loading && (
        <div className="grid" aria-hidden="true">
          {Array.from({ length: 8 }).map((_, i) => (
            <div key={i} className="card skeleton-card">
              <div className="skeleton skeleton-img" />
              <div className="skeleton skeleton-line w-60" />
              <div className="skeleton skeleton-line w-40" />
              <div className="skeleton skeleton-line w-80" />
            </div>
          ))}
        </div>
      )}

      {!loading && !error && (
        <div className="results-bar">
          <span className="results-count">
            <strong>{cars.length}</strong> {cars.length === 1 ? 'coche encontrado' : 'coches encontrados'}
          </span>
          {hasFilters && (
            <button type="button" className="btn btn-ghost btn-sm" onClick={clearFilters}>
              Limpiar filtros
            </button>
          )}
        </div>
      )}

      {!loading && !error && cars.length === 0 && (
        <div className="card empty-card">
          <span className="icon-tile"><SearchIcon /></span>
          <p>Ningún coche cumple esos filtros. Prueba a relajarlos o empieza de nuevo.</p>
          {hasFilters && (
            <div className="detail-actions">
              <button type="button" className="btn btn-primary" onClick={clearFilters}>
                Limpiar filtros
              </button>
            </div>
          )}
        </div>
      )}

      {!loading && (
        <div className="grid">
          {cars.map((car) => (
            <CarCard key={car.id} car={car} />
          ))}
        </div>
      )}
    </section>
  );
}
