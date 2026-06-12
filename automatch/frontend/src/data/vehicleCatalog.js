/**
 * Catálogo de referencia de marcas y modelos para el autocompletado del
 * comparador (vehículos personalizados, no necesariamente a la venta en la
 * web). Los datos de vehicleCatalog.json se generan automáticamente a partir
 * de Wikidata (https://query.wikidata.org/sparql) mediante
 * scripts/fetch-vehicle-catalog.mjs - ver ese fichero para el detalle de la
 * consulta SPARQL y la limpieza de nombres.
 *
 * Para regenerar el catálogo con los datos más recientes de Wikidata:
 *   npm run catalog:fetch
 *
 * No pretende ser exhaustivo -hay miles de variantes y generaciones-, por eso
 * los campos de marca y modelo del formulario son de texto libre: cualquier
 * vehículo que no aparezca en las sugerencias se puede escribir igualmente.
 */
import vehicleCatalogData from './vehicleCatalog.json';

export const VEHICLE_CATALOG = vehicleCatalogData;

export const BRAND_NAMES = Object.keys(VEHICLE_CATALOG).sort((a, b) => a.localeCompare(b, 'es'));
