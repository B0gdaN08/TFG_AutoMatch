import { useEffect, useState } from 'react';
import { request, FUEL_LABELS, TYPE_LABELS } from '../api';
import CarCard from '../components/CarCard';
import { GridIcon, SearchIcon } from '../components/icons';

const DGT_LABELS = { CERO: 'CERO', ECO: 'ECO', C: 'C', B: 'B' };

const SORT_OPTIONS = [
  { value: 'price,asc',  label: 'Precio ↑' },
  { value: 'price,desc', label: 'Precio ↓' },
  { value: 'year,desc',  label: 'Más nuevos' },
  { value: 'brand,asc',  label: 'Marca A-Z' },
];

const PAGE_SIZE = 12;

export default function Catalogo() {
  const [cars, setCars] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [search, setSearch] = useState('');
  const [type, setType] = useState('');
  const [fuel, setFuel] = useState('');
  const [minPrice, setMinPrice] = useState('');
  const [maxPrice, setMaxPrice] = useState('');
  const [dgtLabel, setDgtLabel] = useState('');
  const [sort, setSort] = useState('price,asc');

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);

  const hasFilters = Boolean(search || type || fuel || minPrice || maxPrice || dgtLabel);

  function clearFilters() {
    setSearch('');
    setType('');
    setFuel('');
    setMinPrice('');
    setMaxPrice('');
    setDgtLabel('');
    setPage(0);
  }

  // Resetear página al cambiar cualquier filtro u ordenación
  function handleSearch(v)    { setSearch(v);   setPage(0); }
  function handleType(v)      { setType(v);     setPage(0); }
  function handleFuel(v)      { setFuel(v);     setPage(0); }
  function handleMinPrice(v)  { setMinPrice(v); setPage(0); }
  function handleMaxPrice(v)  { setMaxPrice(v); setPage(0); }
  function handleDgtLabel(v)  { setDgtLabel(v); setPage(0); }
  function handleSort(v)      { setSort(v);     setPage(0); }

  useEffect(() => {
    const params = new URLSearchParams();
    if (search)   params.set('search',   search);
    if (type)     params.set('type',     type);
    if (fuel)     params.set('fuel',     fuel);
    if (minPrice) params.set('minPrice', minPrice);
    if (maxPrice) params.set('maxPrice', maxPrice);
    if (dgtLabel) params.set('dgtLabel', dgtLabel);
    params.set('sort', sort);
    params.set('page', page);
    params.set('size', PAGE_SIZE);

    setLoading(true);
    setError('');
    request(`/cars?${params.toString()}`)
      .then((data) => {
        setCars(data.content);
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
      })
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [search, type, fuel, minPrice, maxPrice, dgtLabel, sort, page]);

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
          onChange={(e) => handleSearch(e.target.value)}
        />
        <select value={type} onChange={(e) => handleType(e.target.value)}>
          <option value="">Todos los tipos</option>
          {Object.entries(TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <select value={fuel} onChange={(e) => handleFuel(e.target.value)}>
          <option value="">Todos los combustibles</option>
          {Object.entries(FUEL_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <select value={dgtLabel} onChange={(e) => handleDgtLabel(e.target.value)}>
          <option value="">Todas las etiquetas DGT</option>
          {Object.entries(DGT_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
        <input
          type="number"
          min="0"
          step="1000"
          placeholder="Precio mínimo (€)"
          value={minPrice}
          onChange={(e) => handleMinPrice(e.target.value)}
        />
        <input
          type="number"
          min="0"
          step="1000"
          placeholder="Precio máximo (€)"
          value={maxPrice}
          onChange={(e) => handleMaxPrice(e.target.value)}
        />
        <select value={sort} onChange={(e) => handleSort(e.target.value)}>
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
      </div>

      {error && <p className="alert">{error}</p>}

      {loading && (
        <div className="grid" aria-hidden="true">
          {Array.from({ length: PAGE_SIZE }).map((_, i) => (
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
            <strong>{totalElements}</strong>{' '}
            {totalElements === 1 ? 'coche encontrado' : 'coches encontrados'}
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

      {!loading && totalPages > 1 && (
        <div className="pagination">
          <button
            className="btn btn-ghost btn-sm"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
          >
            ← Anterior
          </button>
          <span className="pagination-info">
            Página <strong>{page + 1}</strong> de <strong>{totalPages}</strong>
          </span>
          <button
            className="btn btn-ghost btn-sm"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
          >
            Siguiente →
          </button>
        </div>
      )}
    </section>
  );
}
