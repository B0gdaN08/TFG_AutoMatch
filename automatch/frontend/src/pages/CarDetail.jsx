import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { request, FUEL_LABELS, TYPE_LABELS, formatEuro, imageUrl } from '../api';
import { useAuth } from '../AuthContext';
import { addToCompare, removeFromCompare, useCompareIds, MAX_COMPARE } from '../compare';
import Plate from '../components/Plate';
import ScoreBar from '../components/ScoreBar';
import Lightbox from '../components/Lightbox';
import { BoltIcon, FuelIcon, TrunkIcon, UsersIcon } from '../components/icons';

export default function CarDetail() {
  const { id } = useParams();
  const { user } = useAuth();

  const [car, setCar] = useState(null);
  const [tco, setTco] = useState(null);
  const [isFav, setIsFav] = useState(false);
  const [error, setError] = useState('');
  const [activeImage, setActiveImage] = useState(0);
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [compareWarn, setCompareWarn] = useState(false);
  const warnTimer = useRef(null);
  const compareIds = useCompareIds();
  const inCompare = car ? compareIds.includes(car.id) : false;

  useEffect(() => {
    setActiveImage(0);
    request(`/cars/${id}`).then(setCar).catch((e) => setError(e.message));
    request(`/tco/${id}?years=5&km=15000`).then(setTco).catch(() => {});
    if (user) {
      request('/favorites')
        .then((favs) => setIsFav(favs.some((c) => c.id === Number(id))))
        .catch(() => {});
    }
  }, [id, user]);

  async function toggleFav() {
    try {
      if (isFav) {
        await request(`/favorites/${id}`, { method: 'DELETE' });
        setIsFav(false);
      } else {
        await request(`/favorites/${id}`, { method: 'POST' });
        setIsFav(true);
      }
    } catch (e) {
      setError(e.message);
    }
  }

  function toggleCompare() {
    if (inCompare) {
      removeFromCompare(car.id);
      return;
    }
    if (!addToCompare(car.id)) {
      setCompareWarn(true);
      clearTimeout(warnTimer.current);
      warnTimer.current = setTimeout(() => setCompareWarn(false), 3000);
    }
  }

  async function contactSeller() {
    try {
      const contact = await request(`/cars/${id}/contact`);
      const subject = encodeURIComponent(`Interesado en tu ${car.brand} ${car.model} en AutoMatch`);
      window.location.href = `mailto:${contact.email}?subject=${subject}`;
    } catch (e) {
      setError(e.message);
    }
  }

  if (error) return <p className="alert">{error}</p>;
  if (!car) return <p className="muted">Cargando ficha…</p>;

  const isElectric = car.fuel === 'ELECTRICO';

  return (
    <section>
      <Link to="/catalogo" className="backlink">← Volver al catálogo</Link>

      {car.imageUrls?.length > 0 ? (
        <div className="detail-gallery">
          <div className="detail-gallery-main is-clickable" onClick={() => setLightboxOpen(true)}>
            <img src={imageUrl(car.imageUrls[activeImage])} alt={`${car.brand} ${car.model}`} />
          </div>
          {car.imageUrls.length > 1 && (
            <div className="detail-gallery-thumbs">
              {car.imageUrls.map((url, i) => (
                <button
                  key={url}
                  type="button"
                  className={`detail-gallery-thumb ${i === activeImage ? 'active' : ''}`}
                  onClick={() => setActiveImage(i)}
                >
                  <img src={imageUrl(url)} alt="" />
                </button>
              ))}
            </div>
          )}
        </div>
      ) : (
        <div className="detail-gallery-empty" aria-hidden="true">🚗</div>
      )}

      {lightboxOpen && (
        <Lightbox images={car.imageUrls} index={activeImage} onClose={() => setLightboxOpen(false)} />
      )}

      <div className="detail-head">
        <Plate text={car.brand} />
        <div>
          <h1>{car.brand} {car.model}</h1>
          <p className="lead">
            {TYPE_LABELS[car.type]} · {FUEL_LABELS[car.fuel]} · {car.year} ·{' '}
            <span className={`chip chip-dgt-${car.dgtLabel}`}>{car.dgtLabel}</span>
          </p>
        </div>
        <div className="detail-price">
          <strong className="price price-lg">{formatEuro(car.price)}</strong>
          {user && (
            <button className="btn btn-ghost" onClick={toggleFav}>
              {isFav ? '★ Quitar de favoritos' : '☆ Guardar en favoritos'}
            </button>
          )}
        </div>
      </div>

      <div className="detail-quickspecs">
        <div className="quickspec">
          <span className="icon-tile"><BoltIcon /></span>
          <div>
            <span className="quickspec-value">{car.powerCv} CV</span>
            <span className="quickspec-label">Potencia</span>
          </div>
        </div>
        <div className="quickspec">
          <span className="icon-tile"><FuelIcon /></span>
          <div>
            <span className="quickspec-value">{car.consumption} {isElectric ? 'kWh' : 'l'}/100</span>
            <span className="quickspec-label">Consumo</span>
          </div>
        </div>
        <div className="quickspec">
          <span className="icon-tile"><TrunkIcon /></span>
          <div>
            <span className="quickspec-value">{car.trunkLiters} l</span>
            <span className="quickspec-label">Maletero</span>
          </div>
        </div>
        <div className="quickspec">
          <span className="icon-tile"><UsersIcon /></span>
          <div>
            <span className="quickspec-value">{car.seats}</span>
            <span className="quickspec-label">Plazas</span>
          </div>
        </div>
      </div>

      <p className="detail-description">{car.description}</p>

      <div className="detail-grid">
        <div className="card">
          <h2>Ficha técnica</h2>
          <dl className="specs">
            <div><dt>Año</dt><dd>{car.year}</dd></div>
            <div><dt>Tipo</dt><dd>{TYPE_LABELS[car.type]}</dd></div>
            <div><dt>Combustible</dt><dd>{FUEL_LABELS[car.fuel]}</dd></div>
            <div><dt>Etiqueta DGT</dt><dd>{car.dgtLabel}</dd></div>
            <div><dt>Mantenimiento</dt><dd>{formatEuro(car.maintenancePerYear)}/año</dd></div>
            <div><dt>Depreciación 5 años</dt><dd>{Math.round(car.depreciation5y * 100)}%</dd></div>
          </dl>
        </div>

        <div className="card">
          <h2>Puntuaciones</h2>
          <div className="rating-row">
            <span>Fiabilidad</span>
            <ScoreBar value={car.reliability} max={10} tone="blue" />
            <strong>{car.reliability}</strong>
          </div>
          <div className="rating-row">
            <span>Seguridad</span>
            <ScoreBar value={car.safety} max={10} tone="blue" />
            <strong>{car.safety}</strong>
          </div>
          <div className="rating-row">
            <span>Equipamiento</span>
            <ScoreBar value={car.equipment} max={10} tone="blue" />
            <strong>{car.equipment}</strong>
          </div>

          {tco && (
            <div className="tco-mini">
              <h3>Coste real estimado</h3>
              <p>
                <strong>{formatEuro(tco.totalCost)}</strong> en 5 años (15.000 km/año)
                · {formatEuro(tco.costPerYear)}/año
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="detail-actions">
        <Link className="btn btn-primary" to={`/tco?car=${car.id}`}>
          Calcular su coste con tus km
        </Link>
        <button
          type="button"
          className={`btn ${inCompare ? 'btn-compare-active' : 'btn-ghost'}`}
          onClick={toggleCompare}
        >
          {inCompare ? '✓ En el comparador' : 'Añadir al comparador'}
        </button>
        {user && car.userListing && !car.own && (
          <button type="button" className="btn btn-ghost" onClick={contactSeller}>
            ✉ Contactar con el vendedor
          </button>
        )}
      </div>
      {compareWarn && (
        <p className="compare-warn">El comparador admite {MAX_COMPARE} coches como máximo.</p>
      )}
    </section>
  );
}
