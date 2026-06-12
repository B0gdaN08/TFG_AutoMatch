import { useState } from 'react';
import { FUEL_LABELS, TYPE_LABELS, MAX_IMAGES, imageUrl, uploadImage } from '../api';

const DGT_LABELS = ['C', 'ECO', 'CERO'];

const EMPTY_FORM = {
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
  reliability: 5,
  safety: 5,
  equipment: 5,
  maintenancePerYear: '',
  depreciation5y: 50,
  description: '',
  images: [],
};

function toFormValues(car) {
  if (!car) return EMPTY_FORM;
  return {
    brand: car.brand ?? '',
    model: car.model ?? '',
    year: car.year ?? EMPTY_FORM.year,
    price: car.price ?? '',
    type: car.type ?? EMPTY_FORM.type,
    fuel: car.fuel ?? EMPTY_FORM.fuel,
    dgtLabel: car.dgtLabel ?? EMPTY_FORM.dgtLabel,
    consumption: car.consumption ?? '',
    powerCv: car.powerCv ?? '',
    trunkLiters: car.trunkLiters ?? '',
    seats: car.seats ?? EMPTY_FORM.seats,
    reliability: car.reliability ?? EMPTY_FORM.reliability,
    safety: car.safety ?? EMPTY_FORM.safety,
    equipment: car.equipment ?? EMPTY_FORM.equipment,
    maintenancePerYear: car.maintenancePerYear ?? '',
    depreciation5y: car.depreciation5y != null
      ? Math.round(car.depreciation5y * 100)
      : EMPTY_FORM.depreciation5y,
    description: car.description ?? '',
    images: car.imageUrls ?? [],
  };
}

export default function CarForm({ initialValue, onSubmit, submitLabel = 'Guardar', loading = false }) {
  const [values, setValues] = useState(() => toFormValues(initialValue));
  const [uploading, setUploading] = useState(false);
  const [imageError, setImageError] = useState('');

  function setField(field, value) {
    setValues((prev) => ({ ...prev, [field]: value }));
  }

  async function handleFiles(e) {
    const files = Array.from(e.target.files || []);
    e.target.value = '';
    if (!files.length) return;

    const remaining = MAX_IMAGES - values.images.length;
    if (remaining <= 0) {
      setImageError(`Máximo ${MAX_IMAGES} fotos por anuncio.`);
      return;
    }

    setImageError('');
    setUploading(true);
    try {
      const uploaded = [];
      for (const file of files.slice(0, remaining)) {
        const { url } = await uploadImage(file);
        uploaded.push(url);
      }
      setValues((prev) => ({ ...prev, images: [...prev.images, ...uploaded] }));
    } catch (err) {
      setImageError(err.message);
    } finally {
      setUploading(false);
    }
  }

  function removeImage(url) {
    setValues((prev) => ({ ...prev, images: prev.images.filter((u) => u !== url) }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit({
      imageUrls: values.images,
      brand: values.brand,
      model: values.model,
      year: Number(values.year),
      price: Number(values.price),
      type: values.type,
      fuel: values.fuel,
      dgtLabel: values.dgtLabel,
      consumption: Number(values.consumption),
      powerCv: Number(values.powerCv),
      trunkLiters: Number(values.trunkLiters),
      seats: Number(values.seats),
      reliability: Number(values.reliability),
      safety: Number(values.safety),
      equipment: Number(values.equipment),
      maintenancePerYear: Number(values.maintenancePerYear),
      depreciation5y: Number(values.depreciation5y) / 100,
      description: values.description,
    });
  }

  return (
    <form className="form car-form" onSubmit={handleSubmit}>
      <div className="field field-full">
        <span>Fotos ({values.images.length}/{MAX_IMAGES})</span>
        {values.images.length > 0 && (
          <div className="photo-grid">
            {values.images.map((url) => (
              <div className="photo-thumb" key={url}>
                <img src={imageUrl(url)} alt="" />
                <button type="button" className="photo-remove" onClick={() => removeImage(url)} aria-label="Quitar foto">
                  ×
                </button>
              </div>
            ))}
          </div>
        )}
        {values.images.length < MAX_IMAGES && (
          <input type="file" accept="image/*" multiple onChange={handleFiles} disabled={uploading} />
        )}
        {uploading && <p className="muted">Subiendo…</p>}
        {imageError && <p className="alert">{imageError}</p>}
      </div>

      <label className="field">
        Marca
        <input value={values.brand} onChange={(e) => setField('brand', e.target.value)} required />
      </label>
      <label className="field">
        Modelo
        <input value={values.model} onChange={(e) => setField('model', e.target.value)} required />
      </label>
      <label className="field">
        Año
        <input type="number" min="1990" max="2100" value={values.year}
               onChange={(e) => setField('year', e.target.value)} required />
      </label>
      <label className="field">
        Precio (€)
        <input type="number" min="1" max="1000000" step="100" value={values.price}
               onChange={(e) => setField('price', e.target.value)} required />
      </label>
      <label className="field">
        Tipo
        <select value={values.type} onChange={(e) => setField('type', e.target.value)}>
          {Object.entries(TYPE_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Combustible
        <select value={values.fuel} onChange={(e) => setField('fuel', e.target.value)}>
          {Object.entries(FUEL_LABELS).map(([value, label]) => (
            <option key={value} value={value}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Etiqueta DGT
        <select value={values.dgtLabel} onChange={(e) => setField('dgtLabel', e.target.value)}>
          {DGT_LABELS.map((label) => (
            <option key={label} value={label}>{label}</option>
          ))}
        </select>
      </label>
      <label className="field">
        Consumo ({values.fuel === 'ELECTRICO' ? 'kWh' : 'l'}/100km)
        <input type="number" min="0.1" step="0.1" value={values.consumption}
               onChange={(e) => setField('consumption', e.target.value)} required />
      </label>
      <label className="field">
        Potencia (CV)
        <input type="number" min="1" max="2000" step="1" value={values.powerCv}
               onChange={(e) => setField('powerCv', e.target.value)} required />
      </label>
      <label className="field">
        Maletero (l)
        <input type="number" min="1" step="1" value={values.trunkLiters}
               onChange={(e) => setField('trunkLiters', e.target.value)} required />
      </label>
      <label className="field">
        Plazas
        <input type="number" min="2" max="9" step="1" value={values.seats}
               onChange={(e) => setField('seats', e.target.value)} required />
      </label>
      <label className="field">
        Fiabilidad (1-10)
        <input type="number" min="1" max="10" step="0.1" value={values.reliability}
               onChange={(e) => setField('reliability', e.target.value)} required />
      </label>
      <label className="field">
        Seguridad (1-10)
        <input type="number" min="1" max="10" step="0.1" value={values.safety}
               onChange={(e) => setField('safety', e.target.value)} required />
      </label>
      <label className="field">
        Equipamiento (1-10)
        <input type="number" min="1" max="10" step="0.1" value={values.equipment}
               onChange={(e) => setField('equipment', e.target.value)} required />
      </label>
      <label className="field">
        Mantenimiento (€/año)
        <input type="number" min="0" step="10" value={values.maintenancePerYear}
               onChange={(e) => setField('maintenancePerYear', e.target.value)} required />
      </label>
      <label className="field">
        Depreciación a 5 años (%)
        <input type="number" min="0" max="100" step="1" value={values.depreciation5y}
               onChange={(e) => setField('depreciation5y', e.target.value)} required />
      </label>
      <label className="field field-full">
        Descripción
        <textarea rows="3" maxLength="600" value={values.description}
                  onChange={(e) => setField('description', e.target.value)} />
      </label>

      <button className="btn btn-primary field-full" disabled={loading}>
        {loading ? 'Guardando…' : submitLabel}
      </button>
    </form>
  );
}
