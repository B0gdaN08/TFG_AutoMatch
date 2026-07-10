import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { request } from '../api';
import CarCard from '../components/CarCard';
import Reveal from '../components/Reveal';
import CountUp from '../components/CountUp';
import { GridIcon, CompareIcon, ReceiptIcon, TargetIcon } from '../components/icons';

const TOOLS = [
  {
    to: '/catalogo',
    icon: <GridIcon />,
    title: 'Catálogo',
    text: 'Todos los modelos del mercado con filtros por tipo, combustible y precio.',
    action: 'Explorar',
  },
  {
    to: '/comparador',
    icon: <CompareIcon />,
    title: 'Comparador',
    text: 'Dos o tres coches cara a cara, con el mejor dato de cada fila resaltado.',
    action: 'Comparar',
  },
  {
    to: '/tco',
    icon: <ReceiptIcon />,
    title: 'Coste real',
    text: 'Depreciación, energía, seguro, mantenimiento e impuestos: la cifra completa.',
    action: 'Calcular',
  },
  {
    to: '/recomendador',
    icon: <TargetIcon />,
    title: 'Recomendador',
    text: 'Pondera lo que te importa y obtén un ranking explicado criterio a criterio.',
    action: 'Empezar',
  },
];

const STEPS = [
  {
    title: 'Filtra',
    text: 'Acota por presupuesto, tipo de coche y etiqueta DGT. Quédate solo con lo que encaja en tu vida.',
  },
  {
    title: 'Compara',
    text: 'Ponlos cara a cara y mira el coste real a varios años, no solo el precio del concesionario.',
  },
  {
    title: 'Decide',
    text: 'El recomendador ordena tus finalistas según tus prioridades y te explica el porqué de cada puesto.',
  },
];

/** Elige tres coches destacados: el más barato, el eléctrico mejor de precio y el más seguro. */
function buildPicks(cars) {
  if (!cars || cars.length === 0) return [];

  const byPrice = [...cars].sort((a, b) => a.price - b.price);
  const picks = [];
  const used = new Set();

  function add(tag, car) {
    if (car && !used.has(car.id)) {
      used.add(car.id);
      picks.push({ tag, car });
    }
  }

  add('El más barato', byPrice[0]);

  const electric = byPrice.find((c) => c.fuel === 'ELECTRICO' && !used.has(c.id));
  add('Eléctrico al mejor precio', electric);

  const safest = [...cars]
    .filter((c) => !used.has(c.id))
    .sort((a, b) => b.safety - a.safety || a.price - b.price)[0];
  add('El más seguro', safest);

  return picks;
}

export default function Home() {
  const [cars, setCars] = useState(null); // null = cargando

  useEffect(() => {
    request('/cars/all')
      .then(setCars)
      .catch(() => setCars([]));
  }, []);

  const picks = buildPicks(cars);
  const total = cars?.length;

  return (
    <div className="home">
      {/* ----------------------------- hero ----------------------------- */}
      <section className="hero">
        <div className="hero-copy">
          <p className="eyebrow eyebrow-light">Decide con datos · Mercado español</p>
          <h1 className="hero-title">
            ¿Qué coche <em>te compras?</em>
          </h1>
          <p className="hero-lead">
            Compara precios, calcula lo que cada coche cuesta de verdad a varios
            años y deja que un recomendador transparente ordene tus opciones.
          </p>
          <div className="hero-actions">
            <Link className="btn btn-primary btn-lg" to="/recomendador">
              Encuentra tu coche
            </Link>
            <Link className="btn btn-light btn-lg" to="/catalogo">
              Ver catálogo
            </Link>
          </div>
        </div>

        <div className="hero-stats" aria-hidden="true">
          <div className="stat-plate">
            <span className="stat-plate-band">E</span>
            <span className="stat-plate-body">
              <strong><CountUp value={total || 28} /></strong>
              <span>modelos analizados</span>
            </span>
          </div>
          <div className="stat-plate">
            <span className="stat-plate-band">E</span>
            <span className="stat-plate-body">
              <strong><CountUp value={5} /></strong>
              <span>costes en cada cálculo</span>
            </span>
          </div>
          <div className="stat-plate">
            <span className="stat-plate-band">E</span>
            <span className="stat-plate-body">
              <strong><CountUp value={7} /></strong>
              <span>criterios a tu medida</span>
            </span>
          </div>
        </div>

        <div className="hero-road" aria-hidden="true" />
      </section>

      {/* ------------------------- cómo funciona ------------------------ */}
      <Reveal as="section">
        <div className="section-head">
          <p className="eyebrow">Cómo funciona</p>
          <h2 className="section-title">De treinta pestañas a una decisión</h2>
        </div>
        <div className="steps">
          {STEPS.map((step, index) => (
            <Reveal key={step.title} as="article" className="card step" delay={index * 90}>
              <span className="plate step-plate" aria-hidden="true">
                <span className="plate-band">E</span>
                <span className="plate-text">{index + 1}</span>
              </span>
              <h3>{step.title}</h3>
              <p className="muted">{step.text}</p>
            </Reveal>
          ))}
        </div>
      </Reveal>

      {/* -------------------------- herramientas ------------------------ */}
      <Reveal as="section">
        <div className="section-head">
          <p className="eyebrow">Herramientas</p>
          <h2 className="section-title">Cuatro formas de mirar el mismo coche</h2>
        </div>
        <div className="tools">
          {TOOLS.map((tool, index) => (
            <Reveal key={tool.to} as={Link} to={tool.to} className="card tool-card" delay={index * 80}>
              <span className="icon-tile">{tool.icon}</span>
              <h3>{tool.title}</h3>
              <p className="muted">{tool.text}</p>
              <span className="tool-go">
                {tool.action} <span aria-hidden="true">→</span>
              </span>
            </Reveal>
          ))}
        </div>
      </Reveal>

      {/* --------------------------- destacados ------------------------- */}
      <Reveal as="section">
        <div className="section-head">
          <p className="eyebrow">Directo del catálogo</p>
          <h2 className="section-title">Tres formas de empezar</h2>
        </div>

        {cars === null && (
          <div className="picks">
            {Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="card skeleton-card">
                <div className="skeleton skeleton-img" />
                <div className="skeleton skeleton-line w-60" />
                <div className="skeleton skeleton-line w-40" />
                <div className="skeleton skeleton-line w-80" />
              </div>
            ))}
          </div>
        )}

        {cars !== null && picks.length > 0 && (
          <div className="picks">
            {picks.map(({ tag, car }) => (
              <div key={car.id} className="pick">
                <span className="pick-tag">{tag}</span>
                <CarCard car={car} />
              </div>
            ))}
          </div>
        )}

        {cars !== null && picks.length === 0 && (
          <div className="card empty-card">
            <p>
              El catálogo aún está vacío. Arranca el backend para cargar los
              coches de ejemplo.
            </p>
          </div>
        )}
      </Reveal>

      {/* ------------------------------ cta ----------------------------- */}
      <Reveal as="section" className="home-cta">
        <h2 className="section-title">¿Listo para decidir con datos?</h2>
        <p>Tres minutos en el recomendador valen más que treinta pestañas abiertas.</p>
        <Link className="btn btn-primary btn-lg" to="/recomendador">
          Empezar ahora
        </Link>
        <div className="hero-road home-cta-road" aria-hidden="true" />
      </Reveal>
    </div>
  );
}
