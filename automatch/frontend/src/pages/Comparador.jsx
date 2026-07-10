import { useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { request, FUEL_LABELS, formatEuro } from '../api';
import { readCompareIds } from '../compare';
import Plate from '../components/Plate';
import CustomVehicleForm, { EMPTY_CUSTOM_VEHICLE, TYPE_OPTIONS } from '../components/CustomVehicleForm';
import { CompareIcon } from '../components/icons';

/**
 * Coste estimado de recorrer 100 km. Los coches del catálogo ya traen el dato
 * calculado por el backend (CarCosts.java); la fórmula local solo se aplica a
 * los vehículos personalizados, que no pasan por la API.
 */
function energyCost100(car) {
  if (car.energyCostPer100 != null) return car.energyCostPer100;
  switch (car.fuel) {
    case 'DIESEL':
      return car.consumption * 1.55;
    case 'HIBRIDO_ENCHUFABLE':
      return car.consumption * 1.65 * 0.7;
    case 'ELECTRICO':
      return car.consumption * 0.18;
    default: // GASOLINA e HIBRIDO
      return car.consumption * 1.65;
  }
}

function formatEuro2(value) {
  return new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value);
}

const ROWS = [
  { label: 'Precio', value: (c) => c.price, format: (c) => formatEuro(c.price), better: 'min' },
  { label: 'Año', format: (c) => c.year },
  { label: 'Tipo', format: (c) => TYPE_OPTIONS[c.type] || c.type },
  { label: 'Combustible', format: (c) => FUEL_LABELS[c.fuel] || c.fuel },
  { label: 'Etiqueta DGT', format: (c) => c.dgtLabel },
  {
    label: 'Coste por 100 km (estim.)',
    value: (c) => energyCost100(c),
    format: (c) => formatEuro2(energyCost100(c)),
    better: 'min',
  },
  {
    label: 'Consumo',
    format: (c) => `${c.consumption} ${c.fuel === 'ELECTRICO' ? 'kWh' : 'l'}/100`,
  },
  { label: 'Potencia', value: (c) => c.powerCv, format: (c) => `${c.powerCv} CV`, better: 'max' },
  { label: 'Maletero', value: (c) => c.trunkLiters, format: (c) => `${c.trunkLiters} l`, better: 'max' },
  { label: 'Plazas', value: (c) => c.seats, format: (c) => c.seats, better: 'max' },
  {
    label: 'Fiabilidad', value: (c) => c.reliability, format: (c) => `${c.reliability}/10`,
    better: 'max', customUnavailable: true,
  },
  {
    label: 'Seguridad', value: (c) => c.safety, format: (c) => `${c.safety}/10`,
    better: 'max', customUnavailable: true,
  },
  {
    label: 'Equipamiento', value: (c) => c.equipment, format: (c) => `${c.equipment}/10`,
    better: 'max', customUnavailable: true,
  },
  {
    label: 'Mantenimiento/año',
    value: (c) => c.maintenancePerYear,
    format: (c) => formatEuro(c.maintenancePerYear),
    better: 'min',
    customUnavailable: true,
  },
  {
    label: 'Depreciación a 5 años',
    value: (c) => c.depreciation5y,
    format: (c) => `${Math.round(c.depreciation5y * 100)}%`,
    better: 'min',
    customUnavailable: true,
  },
];

export default function Comparador() {
  const [searchParams] = useSearchParams();
  // Sin parámetro ids en la URL, se precarga la selección hecha con los botones "Comparar"
  const initialIds = (searchParams.get('ids') || readCompareIds().join(','))
    .split(',')
    .filter(Boolean)
    .slice(0, 3);

  const [allCars, setAllCars] = useState([]);
  const [slots, setSlots] = useState([initialIds[0] || '', initialIds[1] || '', initialIds[2] || '']);
  const [customVehicles, setCustomVehicles] = useState([null, null, null]);
  const [error, setError] = useState('');

  useEffect(() => {
    request('/cars/all')
      .then(setAllCars)
      .catch((e) => setError(e.message));
  }, []);

  function setSlot(index, value) {
    setSlots((prev) => prev.map((v, i) => (i === index ? value : v)));
  }

  function setCustomVehicle(index, vehicle) {
    setCustomVehicles((prev) => prev.map((v, i) => (i === index ? vehicle : v)));
  }

  const cars = useMemo(() => {
    return slots
      .map((slot, index) => {
        if (slot === 'custom') {
          const custom = customVehicles[index] || EMPTY_CUSTOM_VEHICLE;
          if (!custom.brand.trim() || !custom.model.trim()) return null;
          return {
            ...custom,
            id: `custom-${index}`,
            year: Number(custom.year) || 0,
            price: Number(custom.price) || 0,
            consumption: Number(custom.consumption) || 0,
            powerCv: Number(custom.powerCv) || 0,
            trunkLiters: Number(custom.trunkLiters) || 0,
            seats: Number(custom.seats) || 0,
            custom: true,
          };
        }
        if (!slot) return null;
        return allCars.find((c) => String(c.id) === slot) || null;
      })
      .filter(Boolean);
  }, [slots, customVehicles, allCars]);

  const hasCustom = cars.some((c) => c.custom);
  const visibleRows = ROWS.filter((row) => !(hasCustom && row.customUnavailable));

  function bestIndex(row) {
    if (!row.better || cars.length === 0) return -1;
    let best = 0;
    for (let i = 1; i < cars.length; i++) {
      const a = row.value(cars[i]);
      const b = row.value(cars[best]);
      if ((row.better === 'min' && a < b) || (row.better === 'max' && a > b)) {
        best = i;
      }
    }
    return best;
  }

  return (
    <section>
      <div className="page-hero">
        <span className="page-hero-icon"><CompareIcon /></span>
        <div className="page-hero-body">
          <p className="eyebrow">Comparador</p>
          <h1>Cara a cara</h1>
          <p className="lead">
            Elige dos o tres vehículos y compáralos punto por punto. Puedes
            seleccionar coches del catálogo o introducir cualquier otro vehículo
            a mano, aunque no esté a la venta en la web. El mejor valor de cada
            fila aparece resaltado.
          </p>
        </div>
      </div>

      <div className="filters card compare-slots">
        {slots.map((value, index) => (
          <div className="compare-slot" key={index}>
            <select value={value} onChange={(e) => setSlot(index, e.target.value)}>
              <option value="">
                {index === 2 ? 'Coche 3 (opcional)' : `Coche ${index + 1}`}
              </option>
              <option value="custom">🚗 Vehículo personalizado</option>
              {allCars.map((car) => (
                <option key={car.id} value={car.id}>
                  {car.brand} {car.model} · {formatEuro(car.price)}
                </option>
              ))}
            </select>
            {value === 'custom' && (
              <CustomVehicleForm
                value={customVehicles[index] || EMPTY_CUSTOM_VEHICLE}
                onChange={(v) => setCustomVehicle(index, v)}
                formId={`custom-${index}`}
              />
            )}
          </div>
        ))}
      </div>

      {error && <p className="alert">{error}</p>}

      {cars.length < 2 && !error && (
        <div className="card empty-card">
          <span className="icon-tile"><CompareIcon /></span>
          <p>
            Selecciona al menos dos vehículos arriba para verlos cara a cara,
            con el mejor dato de cada fila resaltado en ámbar.
          </p>
        </div>
      )}

      {cars.length >= 2 && (
        <div className="card table-wrap">
          <table className="compare-table">
            <thead>
              <tr>
                <th></th>
                {cars.map((car) => (
                  <th key={car.id}>
                    <Plate text={car.brand} />
                    <div className="compare-name">{car.brand} {car.model}</div>
                    {car.custom ? (
                      <span className="compare-link muted">Vehículo personalizado</span>
                    ) : (
                      <Link className="compare-link" to={`/coches/${car.id}`}>Ver ficha</Link>
                    )}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {visibleRows.map((row) => {
                const best = bestIndex(row);
                return (
                  <tr key={row.label}>
                    <th scope="row">{row.label}</th>
                    {cars.map((car, index) => (
                      <td key={car.id} className={index === best ? 'best' : ''}>
                        {row.format(car)}
                      </td>
                    ))}
                  </tr>
                );
              })}
            </tbody>
          </table>
          {hasCustom && (
            <p className="muted compare-note">
              Para vehículos personalizados no se muestran fiabilidad, seguridad,
              equipamiento, mantenimiento ni depreciación: son estimaciones propias
              del catálogo y no existen para coches que no están en la web.
            </p>
          )}
        </div>
      )}
    </section>
  );
}
