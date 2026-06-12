import { FUEL_LABELS, TYPE_LABELS } from '../api';
import { VEHICLE_CATALOG, BRAND_NAMES } from '../data/vehicleCatalog';

const DGT_OPTIONS = ['Sin etiqueta', 'B', 'C', 'ECO', 'CERO'];

const EXTRA_TYPES = {
  MONOVOLUMEN: 'Monovolumen',
  COUPE: 'Coupé',
  DESCAPOTABLE: 'Descapotable',
  PICKUP: 'Pick-up',
  FURGONETA: 'Furgoneta',
};

export const TYPE_OPTIONS = { ...TYPE_LABELS, ...EXTRA_TYPES };

export const EMPTY_CUSTOM_VEHICLE = {
  brand: '',
  model: '',
  year: new Date().getFullYear(),
  price: '',
  type: 'URBANO',
  fuel: 'GASOLINA',
  dgtLabel: 'C',
  consumption: '',
  powerCv: '',
  trunkLiters: '',
  seats: 5,
};

/** Formulario compacto para introducir un vehículo que no está publicado en la web. */
export default function CustomVehicleForm({ value, onChange, formId }) {
  function setField(field, fieldValue) {
    onChange({ ...value, [field]: fieldValue });
  }

  const brandKey = BRAND_NAMES.find((b) => b.toLowerCase() === value.brand.trim().toLowerCase());
  const models = brandKey ? VEHICLE_CATALOG[brandKey] : [];

  return (
    <div className="custom-vehicle-form">
      <label className="field">
        Marca
        <input
          list={`${formId}-brands`}
          value={value.brand}
          onChange={(e) => setField('brand', e.target.value)}
          placeholder="ej. Toyota"
        />
        <datalist id={`${formId}-brands`}>
          {BRAND_NAMES.map((b) => <option key={b} value={b} />)}
        </datalist>
      </label>
      <label className="field">
        Modelo
        <input
          list={`${formId}-models`}
          value={value.model}
          onChange={(e) => setField('model', e.target.value)}
          placeholder="ej. Corolla"
        />
        <datalist id={`${formId}-models`}>
          {models.map((m) => <option key={m} value={m} />)}
        </datalist>
      </label>
      <label className="field">
        Año
        <input type="number" min="1950" max="2100" value={value.year}
               onChange={(e) => setField('year', e.target.value)} />
      </label>
      <label className="field">
        Precio (€)
        <input type="number" min="0" step="100" value={value.price}
               onChange={(e) => setField('price', e.target.value)} />
      </label>
      <label className="field">
        Tipo
        <select value={value.type} onChange={(e) => setField('type', e.target.value)}>
          {Object.entries(TYPE_OPTIONS).map(([v, label]) => (
            <option key={v} value={v}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Combustible
        <select value={value.fuel} onChange={(e) => setField('fuel', e.target.value)}>
          {Object.entries(FUEL_LABELS).map(([v, label]) => (
            <option key={v} value={v}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Etiqueta DGT
        <select value={value.dgtLabel} onChange={(e) => setField('dgtLabel', e.target.value)}>
          {DGT_OPTIONS.map((label) => (
            <option key={label} value={label}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Consumo ({value.fuel === 'ELECTRICO' ? 'kWh' : 'l'}/100km)
        <input type="number" min="0" step="0.1" value={value.consumption}
               onChange={(e) => setField('consumption', e.target.value)} />
      </label>
      <label className="field">
        Potencia (CV)
        <input type="number" min="0" step="1" value={value.powerCv}
               onChange={(e) => setField('powerCv', e.target.value)} />
      </label>
      <label className="field">
        Maletero (l)
        <input type="number" min="0" step="1" value={value.trunkLiters}
               onChange={(e) => setField('trunkLiters', e.target.value)} />
      </label>
      <label className="field">
        Plazas
        <input type="number" min="1" max="9" step="1" value={value.seats}
               onChange={(e) => setField('seats', e.target.value)} />
      </label>
    </div>
  );
}
