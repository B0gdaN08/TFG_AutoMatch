#!/usr/bin/env node
/**
 * Genera src/data/vehicleCatalog.json a partir de datos REALES de Wikidata
 * (https://query.wikidata.org/sparql), la base de datos abierta que alimenta
 * los infoboxes de Wikipedia. Se usa para el autocompletado de marca/modelo
 * del comparador (cualquier vehículo, esté o no a la venta en la web).
 *
 * Cómo se ha construido este script (para la presentación del TFG):
 *
 *  1. Modo `--discover`: lanza una consulta SPARQL que cuenta, para cada
 *     entidad "fabricante" de Wikidata, cuántos "modelo de automóvil"
 *     (Q3231690) tiene asociados mediante la propiedad P176 (fabricante).
 *     Esto es lo que se usó para DESCUBRIR qué marcas tienen cobertura en
 *     Wikidata y elaborar la lista MANUFACTURERS de abajo a mano: se
 *     descartaron entidades sin etiqueta (Qxxxxx) y fabricantes no
 *     relacionados con coches de turismo actuales (camiones, bicicletas,
 *     marcas históricas muy minoritarias). Para Volkswagen y Mercedes-Benz se
 *     consultan DOS entidades (la marca y su holding "Group"): en Wikidata
 *     los modelos históricos están etiquetados con la marca, pero los
 *     actuales (Golf VIII, ID.3/4/5/7, Clase EQA-EQS, GLC/GLE/GLS...) están
 *     etiquetados con el holding. Ambas consultas acaban en la misma marca
 *     del catálogo porque comparten el mismo prefijo de nombre ("Volkswagen
 *     ...", "Mercedes-Benz ..."). Se puede volver a ejecutar en cualquier
 *     momento y el resultado es reproducible (los datos de Wikidata cambian
 *     con el tiempo, pero el método es el mismo).
 *
 *  2. Modo normal (por defecto): para cada fabricante de MANUFACTURERS lanza
 *     una segunda consulta SPARQL que pide sus modelos reales (con etiqueta
 *     en español o, si no existe, en inglés). Limpia cada nombre (quita el
 *     prefijo de la marca, sufijos de generación en números romanos, "Mk2",
 *     paréntesis con el código de chasis, variantes "concept"/"prototype"...)
 *     y construye el catálogo final marca -> [modelos]. Algunos modelos
 *     aparecen con el prefijo de OTRA marca (p.ej. "Lexus GS" dentro de los
 *     resultados de Toyota, o "Cupra Ateca" dentro de los de SEAT) porque en
 *     Wikidata esas sub-marcas comparten fabricante; el script los detecta y
 *     los coloca bajo su propia marca (SUB_BRANDS).
 *
 *  3. Escribe el resultado en src/data/vehicleCatalog.json, que
 *     src/data/vehicleCatalog.js importa y reexpone como VEHICLE_CATALOG /
 *     BRAND_NAMES.
 *
 * Uso:
 *   node scripts/fetch-vehicle-catalog.mjs --discover   # solo explorar/mostrar
 *   node scripts/fetch-vehicle-catalog.mjs              # generar el catálogo
 *
 * (también disponibles como `npm run catalog:discover` / `npm run catalog:fetch`)
 */

import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const OUT_PATH = path.join(__dirname, '..', 'src', 'data', 'vehicleCatalog.json');

const ENDPOINT = 'https://query.wikidata.org/sparql';
const USER_AGENT = 'AutomatchTFG/1.0 (educational project; contact: bogdan.maran.bm@gmail.com)';

// "Modelo de automóvil" en Wikidata.
const AUTOMOBILE_MODEL = 'Q3231690';

/**
 * Fabricantes a consultar: { qid, name, prefix? }.
 *  - `name`  es el nombre de marca que aparecerá en el catálogo.
 *  - `prefix` es el texto con el que empiezan las etiquetas de sus modelos en
 *    Wikidata, si difiere de `name` (p.ej. la entidad se llama "Audi AG" pero
 *    sus modelos se llaman "Audi A3", "Audi Q5"...). Por defecto = `name`.
 *
 * QIDs obtenidos a partir del resultado de `--discover` (fabricantes con más
 * modelos de "automobile model" en Wikidata), agrupados por origen.
 */
const MANUFACTURERS = [
  // --- Generalistas europeos ---
  { qid: 'Q246', name: 'Volkswagen' },
  // "Volkswagen Group": aquí viven los modelos actuales (Golf VIII, ID.3/4/5/6/7,
  // Polo, Passat, Tiguan, up!, Touran, Caddy, Amarok, Taigun...).
  { qid: 'Q156578', name: 'Volkswagen' },
  { qid: 'Q44294', name: 'Ford', prefix: 'Ford' },
  { qid: 'Q6686', name: 'Renault' },
  { qid: 'Q6742', name: 'Peugeot' },
  { qid: 'Q6746', name: 'Citroën' },
  { qid: 'Q40966', name: 'Opel' },
  { qid: 'Q27597', name: 'Fiat' },
  { qid: 'Q27460', name: 'Dacia' },
  { qid: 'Q188217', name: 'SEAT' },
  { qid: 'Q29637', name: 'Škoda', prefix: 'Škoda' },
  { qid: 'Q26823', name: 'Abarth' },
  { qid: 'Q26921', name: 'Alfa Romeo' },
  { qid: 'Q35896', name: 'Lancia' },
  { qid: 'Q26944', name: 'Alpine' },
  { qid: 'Q16040593', name: 'DS', prefix: 'DS' },
  { qid: 'Q48957', name: 'Saab', prefix: 'Saab' },
  { qid: 'Q215293', name: 'Volvo', prefix: 'Volvo' },
  { qid: 'Q1140388', name: 'Triumph' },

  // --- Premium / lujo alemanes y británicos ---
  { qid: 'Q26678', name: 'BMW' },
  { qid: 'Q36008', name: 'Mercedes-Benz' },
  // "Mercedes-Benz Group" (antes Daimler): modelos actuales (EQA-EQS, Clase
  // GLA-GLS, Clase V/X...) están etiquetados aquí en lugar de en Q36008.
  { qid: 'Q27530', name: 'Mercedes-Benz' },
  { qid: 'Q23317', name: 'Audi', prefix: 'Audi' },
  { qid: 'Q40993', name: 'Porsche' },
  { qid: 'Q30055', name: 'Jaguar', prefix: 'Jaguar' },
  { qid: 'Q35907', name: 'Land Rover' },
  { qid: 'Q27224', name: 'Bentley' },
  { qid: 'Q234803', name: 'Rolls-Royce', prefix: 'Rolls-Royce' },
  { qid: 'Q27074', name: 'Aston Martin', prefix: 'Aston Martin' },
  { qid: 'Q35935', name: 'Lotus', prefix: 'Lotus' },

  // --- Deportivos / superdeportivos ---
  { qid: 'Q27586', name: 'Ferrari' },
  { qid: 'Q35886', name: 'Lamborghini' },
  { qid: 'Q35962', name: 'Maserati' },
  { qid: 'Q27401', name: 'Bugatti' },

  // --- Japoneses y coreanos ---
  { qid: 'Q53268', name: 'Toyota' },
  { qid: 'Q35919', name: 'Lexus' },
  { qid: 'Q27511', name: 'Daihatsu' },
  { qid: 'Q20165', name: 'Nissan' },
  { qid: 'Q9584', name: 'Honda' },
  { qid: 'Q35996', name: 'Mazda' },
  { qid: 'Q181642', name: 'Suzuki', prefix: 'Suzuki' },
  { qid: 'Q36033', name: 'Mitsubishi', prefix: 'Mitsubishi' },
  { qid: 'Q172741', name: 'Subaru' },
  { qid: 'Q29803', name: 'Isuzu' },
  { qid: 'Q27543', name: 'Datsun' },
  { qid: 'Q55931', name: 'Hyundai', prefix: 'Hyundai' },
  { qid: 'Q35349', name: 'Kia' },
  { qid: 'Q21451523', name: 'Genesis', prefix: 'Genesis' },

  // --- Estadounidenses ---
  { qid: 'Q29570', name: 'Chevrolet' },
  { qid: 'Q27436', name: 'Cadillac' },
  { qid: 'Q27564', name: 'Dodge' },
  { qid: 'Q30113', name: 'Jeep' },
  { qid: 'Q216796', name: 'Lincoln', prefix: 'Lincoln' },
  { qid: 'Q478214', name: 'Tesla' },

  // --- Eléctricos / nuevos ---
  { qid: 'Q4047174', name: 'Polestar' },
  { qid: 'Q156490', name: 'Smart', prefix: 'Smart' },

  // --- Chinos ---
  { qid: 'Q27423', name: 'BYD', prefix: 'BYD' },
  { qid: 'Q591001', name: 'Chery', prefix: 'Chery' },
  { qid: 'Q1117001', name: 'Great Wall', prefix: 'Great Wall' },
  { qid: 'Q29366', name: 'Changan', prefix: 'Changan' },
  { qid: 'Q739000', name: 'Geely' },
  { qid: 'Q1881443', name: 'MG', prefix: 'MG' },
  { qid: 'Q1124351', name: 'FAW', prefix: 'FAW' },
  { qid: 'Q545579', name: 'JAC', prefix: 'JAC' },

  // --- Indios / otros ---
  { qid: 'Q188514', name: 'Tata Motors', prefix: 'Tata' },
  { qid: 'Q848059', name: 'Mahindra', prefix: 'Mahindra' },
  { qid: 'Q827306', name: 'Proton' },
  { qid: 'Q2309', name: 'Lada', prefix: 'Lada' },
];

/**
 * Marcas que en Wikidata comparten fabricante (P176) con otra marca matriz,
 * por lo que sus modelos aparecen mezclados en los resultados de esa marca
 * (p.ej. "Mini Cooper" dentro de los resultados de BMW, o "Infiniti Q50"
 * dentro de los de Nissan). El script las extrae como marcas propias.
 */
const SUB_BRANDS = [
  { name: 'Mini' },
  { name: 'Cupra' },
  { name: 'Infiniti' },
];

const ALL_BRANDS = [...MANUFACTURERS, ...SUB_BRANDS];
// Prefijos ordenados de más largo a más corto, para que "Land Rover" se
// compruebe antes que un hipotético "Land" y evitar coincidencias parciales.
const PREFIXES_BY_LENGTH = [...new Set(ALL_BRANDS.map((b) => b.prefix || b.name))].sort(
  (a, b) => b.length - a.length,
);
const BRAND_BY_PREFIX = new Map(ALL_BRANDS.map((b) => [b.prefix || b.name, b.name]));

const TRAILING_PARENS = /\s*\([^)]*\)\s*$/;
const MK_SUFFIX = /\s+Mk\.?\s*\d+$/i;
const ROMAN_SUFFIX = /\s+(VIII|VII|VI|IV|V|III|II|I)$/;
const PURE_ROMAN = /^(VIII|VII|VI|IV|V|III|II|I)$/;
const BAD_WORDS = /\b(concept|prototype|facelift|study|edition|abt)\b/i;
// Códigos de fábrica históricos tipo "Type AG-1", "Type B"... (p.ej. los
// primeros Renault de principios del s. XX), no son nombres comerciales.
const TYPE_CODE = /^Type\s+[A-Za-z0-9]/;
// Etiquetas que, tras quitar el prefijo de marca, quedan en una única palabra
// genérica de categoría sin modelo concreto (p.ej. "Renault Type" o
// "Mercedes-Benz Clase", residuos de entidades de Wikidata más genéricas que
// un modelo real).
const GENERIC_LABEL = /^(type|clase|class|series|serie|tipo)$/i;

function cleanModelName(raw) {
  let s = raw.trim();
  s = s.replace(TRAILING_PARENS, '');
  s = s.replace(MK_SUFFIX, '');
  s = s.replace(ROMAN_SUFFIX, '');
  s = s.trim();
  if (!s || PURE_ROMAN.test(s) || BAD_WORDS.test(s) || TYPE_CODE.test(s) || GENERIC_LABEL.test(s)) return null;
  return s;
}

/**
 * Decide a qué marca y con qué nombre de modelo corresponde una etiqueta de
 * Wikidata (p.ej. "Toyota Corolla", "MG4" o "Cupra Ateca").
 */
function routeLabel(raw, ownPrefix) {
  if (raw.startsWith(`${ownPrefix} `)) {
    return { brand: BRAND_BY_PREFIX.get(ownPrefix), model: cleanModelName(raw.slice(ownPrefix.length + 1)) };
  }
  // Modelos sin espacio tras la marca, p.ej. "MG4", "MG5".
  if (raw.startsWith(ownPrefix) && raw.length > ownPrefix.length && /[0-9A-Za-zÀ-ÿ]/.test(raw[ownPrefix.length])) {
    return { brand: BRAND_BY_PREFIX.get(ownPrefix), model: cleanModelName(raw) };
  }
  for (const prefix of PREFIXES_BY_LENGTH) {
    if (prefix === ownPrefix) continue;
    if (raw.startsWith(`${prefix} `)) {
      return { brand: BRAND_BY_PREFIX.get(prefix), model: cleanModelName(raw.slice(prefix.length + 1)) };
    }
  }
  return null;
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Ejecuta una consulta SPARQL contra Wikidata, con reintentos ante 429. */
async function sparql(query) {
  const url = `${ENDPOINT}?${new URLSearchParams({ query, format: 'json' })}`;
  for (let attempt = 1; attempt <= 5; attempt++) {
    const res = await fetch(url, {
      headers: {
        'User-Agent': USER_AGENT,
        Accept: 'application/sparql-results+json',
      },
    });
    if (res.status === 429) {
      const retryAfter = Number(res.headers.get('retry-after')) || 5;
      console.log(`    -> 429 Too Many Requests, esperando ${retryAfter}s (intento ${attempt}/5)...`);
      await sleep(retryAfter * 1000);
      continue;
    }
    if (res.status >= 500) {
      console.log(`    -> HTTP ${res.status}, reintentando en 3s (intento ${attempt}/5)...`);
      await sleep(3000);
      continue;
    }
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${(await res.text()).slice(0, 200)}`);
    }
    const data = await res.json();
    return data.results.bindings;
  }
  throw new Error('Demasiados reintentos (429)');
}

function modelsQuery(qid) {
  return `
SELECT DISTINCT ?modelLabel WHERE {
  ?model wdt:P31 wd:${AUTOMOBILE_MODEL};
         wdt:P176 wd:${qid}.
  OPTIONAL { ?model wdt:P571 ?inception. }
  BIND(COALESCE(?inception, "1900-01-01"^^xsd:dateTime) AS ?sortDate)
  OPTIONAL { ?model rdfs:label ?esLabel. FILTER(LANG(?esLabel) = "es") }
  OPTIONAL { ?model rdfs:label ?enLabel. FILTER(LANG(?enLabel) = "en") }
  BIND(COALESCE(?esLabel, ?enLabel) AS ?modelLabel)
  FILTER(BOUND(?modelLabel))
}
ORDER BY DESC(?sortDate)
LIMIT 150`;
}

const DISCOVER_QUERY = `
SELECT ?manufacturer ?manufacturerLabel (COUNT(?model) AS ?count) WHERE {
  ?model wdt:P31 wd:${AUTOMOBILE_MODEL}.
  ?model wdt:P176 ?manufacturer.
  SERVICE wikibase:label { bd:serviceParam wikibase:language "es,en". }
}
GROUP BY ?manufacturer ?manufacturerLabel
ORDER BY DESC(?count)
LIMIT 200`;

async function runDiscovery() {
  console.log('Consultando Wikidata: fabricantes con más "modelo de automóvil" (P176) registrados...\n');
  const rows = await sparql(DISCOVER_QUERY);
  for (const row of rows) {
    const qid = row.manufacturer.value.split('/').pop();
    console.log(`${row.count.value.padStart(6)}  ${row.manufacturerLabel.value} (${qid})`);
  }
  console.log(`\n${rows.length} fabricantes. Esta lista es la base de la que se ha extraído MANUFACTURERS.`);
}

async function runFetch() {
  const catalog = new Map(); // brand name -> Map(lowercase -> displayName)

  for (const [i, manufacturer] of MANUFACTURERS.entries()) {
    const prefix = manufacturer.prefix || manufacturer.name;
    process.stdout.write(`[${i + 1}/${MANUFACTURERS.length}] ${manufacturer.name} (${manufacturer.qid})... `);
    let rows;
    try {
      rows = await sparql(modelsQuery(manufacturer.qid));
    } catch (err) {
      console.log(`ERROR: ${err.message}`);
      continue;
    }

    let accepted = 0;
    for (const row of rows) {
      const routed = routeLabel(row.modelLabel.value, prefix);
      if (!routed || !routed.model) continue;
      if (!catalog.has(routed.brand)) catalog.set(routed.brand, new Map());
      const bucket = catalog.get(routed.brand);
      const key = routed.model.toLowerCase();
      if (!bucket.has(key)) {
        bucket.set(key, routed.model);
        accepted++;
      }
    }
    console.log(`${rows.length} modelos recibidos -> ${accepted} nuevos`);
    await sleep(400);
  }

  const result = {};
  for (const [brand, bucket] of catalog) {
    if (bucket.size === 0) continue;
    result[brand] = [...bucket.values()].sort((a, b) => a.localeCompare(b, 'es'));
  }

  const sortedResult = Object.fromEntries(
    Object.entries(result).sort(([a], [b]) => a.localeCompare(b, 'es')),
  );

  fs.writeFileSync(OUT_PATH, `${JSON.stringify(sortedResult, null, 2)}\n`, 'utf-8');

  const totalModels = Object.values(sortedResult).reduce((sum, arr) => sum + arr.length, 0);
  console.log(`\nEscrito ${path.relative(process.cwd(), OUT_PATH)}`);
  console.log(`${Object.keys(sortedResult).length} marcas, ${totalModels} modelos en total.`);
}

if (process.argv.includes('--discover')) {
  await runDiscovery();
} else {
  await runFetch();
}
