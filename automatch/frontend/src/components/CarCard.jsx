import { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { FUEL_LABELS, TYPE_LABELS, STATUS_LABELS, formatEuro, imageUrl } from '../api';
import { addToCompare, removeFromCompare, useCompareIds, MAX_COMPARE } from '../compare';
import Plate from './Plate';
import Lightbox from './Lightbox';

export default function CarCard({ car, onRemove, removeLabel = 'Quitar', onEdit }) {
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const [compareWarn, setCompareWarn] = useState(false);
  const warnTimer = useRef(null);
  const compareIds = useCompareIds();
  const inCompare = compareIds.includes(car.id);
  const hasImages = car.imageUrls?.length > 0;

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

  return (
    <article className="card car-card">
      <div
        className={`car-card-image ${hasImages ? 'is-clickable' : ''}`}
        onClick={() => hasImages && setLightboxOpen(true)}
      >
        {hasImages ? (
          <img src={imageUrl(car.imageUrls[0])} alt={`${car.brand} ${car.model}`} />
        ) : (
          <div className="car-card-image-placeholder" aria-hidden="true">🚗</div>
        )}
      </div>
      {lightboxOpen && (
        <Lightbox images={car.imageUrls} onClose={() => setLightboxOpen(false)} />
      )}

      <div className="car-card-head">
        <Plate text={car.brand} />
        <div className="car-card-chips">
          {car.status && car.status !== 'APPROVED' && (
            <span className={`chip chip-status-${car.status.toLowerCase()}`}>
              {STATUS_LABELS[car.status]}
            </span>
          )}
          <span className={`chip chip-dgt-${car.dgtLabel}`}>{car.dgtLabel}</span>
        </div>
      </div>

      <h3 className="car-card-title">{car.brand} {car.model}</h3>
      <p className="car-card-sub">
        {TYPE_LABELS[car.type]} · {FUEL_LABELS[car.fuel]} · {car.year}
      </p>

      <div className="car-card-specs">
        <span title="Potencia">{car.powerCv} CV</span>
        <span title="Consumo">
          {car.consumption} {car.fuel === 'ELECTRICO' ? 'kWh' : 'l'}/100
        </span>
        <span title="Maletero">{car.trunkLiters} l</span>
        <span title="Plazas">{car.seats} plazas</span>
      </div>

      <div className="car-card-footer">
        <strong className="price">{formatEuro(car.price)}</strong>
        <div className="car-card-actions">
          {onEdit && (
            <button className="btn btn-ghost btn-sm" onClick={() => onEdit(car)}>
              Editar
            </button>
          )}
          {onRemove && (
            <button className="btn btn-ghost btn-sm" onClick={() => onRemove(car)}>
              {removeLabel}
            </button>
          )}
          <button
            className={`btn btn-sm ${inCompare ? 'btn-compare-active' : 'btn-ghost'}`}
            onClick={toggleCompare}
            title={inCompare ? 'Quitar del comparador' : 'Añadir al comparador'}
          >
            {inCompare ? '✓ Comparando' : 'Comparar'}
          </button>
          <Link className="btn btn-primary btn-sm" to={`/coches/${car.id}`}>
            Ver ficha
          </Link>
        </div>
      </div>
      {compareWarn && (
        <p className="compare-warn">El comparador admite {MAX_COMPARE} coches como máximo.</p>
      )}
    </article>
  );
}
