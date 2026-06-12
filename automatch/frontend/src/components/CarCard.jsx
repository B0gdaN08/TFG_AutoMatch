import { useState } from 'react';
import { Link } from 'react-router-dom';
import { FUEL_LABELS, TYPE_LABELS, STATUS_LABELS, formatEuro, imageUrl } from '../api';
import Plate from './Plate';
import Lightbox from './Lightbox';

export default function CarCard({ car, onRemove, removeLabel = 'Quitar' }) {
  const [lightboxOpen, setLightboxOpen] = useState(false);
  const hasImages = car.imageUrls?.length > 0;

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
          {onRemove && (
            <button className="btn btn-ghost btn-sm" onClick={() => onRemove(car)}>
              {removeLabel}
            </button>
          )}
          <Link className="btn btn-primary btn-sm" to={`/coches/${car.id}`}>
            Ver ficha
          </Link>
        </div>
      </div>
    </article>
  );
}
