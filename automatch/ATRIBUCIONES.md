# Atribución de imágenes

Las fotografías de los coches del catálogo oficial se encuentran en
`backend/src/main/resources/static/images/cars/real/`. Se declaran como
procedentes de **Wikimedia Commons**, cuyo material suele publicarse bajo
licencias que **exigen atribución** (habitualmente CC BY-SA o CC BY, y algunas
de dominio público).

> ⚠️ **Pendiente de completar antes de publicar o entregar.** Cada fila de la
> tabla debe rellenarse con los datos reales verificados en la página de
> descripción del fichero en Wikimedia Commons (autor, licencia exacta con su
> versión, y URL de origen). Si no puedes verificar la procedencia de alguna
> imagen, sustitúyela por otra con licencia conocida (o por una imagen propia).
> No des por buena una licencia sin comprobarla en la fuente.

## Imágenes del catálogo

| Fichero | Modelo | Autor | Licencia | URL de origen |
|---------|--------|-------|----------|---------------|
| `audi-a3.jpg` | Audi A3 Sportback | | | |
| `bmw-serie-1.jpg` | BMW Serie 1 | | | |
| `citroen-c3.jpg` | Citroën C3 | | | |
| `cupra-formentor.jpg` | Cupra Formentor | | | |
| `dacia-duster.jpg` | Dacia Duster | | | |
| `dacia-jogger.jpg` | Dacia Jogger | | | |
| `dacia-sandero.jpg` | Dacia Sandero | | | |
| `ford-puma.jpg` | Ford Puma | | | |
| `hyundai-tucson.jpg` | Hyundai Tucson | | | |
| `kia-ev3.jpg` | Kia EV3 | | | |
| `kia-sportage.jpg` | Kia Sportage | | | |
| `mazda-3.jpg` | Mazda 3 | | | |
| `mazda-cx-30.jpg` | Mazda CX-30 | | | |
| `mg-mg4.jpg` | MG MG4 | | | |
| `nissan-qashqai.jpg` | Nissan Qashqai | | | |
| `opel-corsa.jpg` | Opel Corsa | | | |
| `peugeot-208.jpg` | Peugeot 208 | | | |
| `peugeot-3008.jpg` | Peugeot 3008 | | | |
| `renault-5.jpg` | Renault 5 E-Tech | | | |
| `renault-clio.jpg` | Renault Clio | | | |
| `seat-ibiza.jpg` | Seat Ibiza | | | |
| `seat-leon.jpg` | Seat León | | | |
| `skoda-kodiaq.jpg` | Skoda Kodiaq | | | |
| `skoda-octavia.jpg` | Skoda Octavia | | | |
| `tesla-model-3.jpg` | Tesla Model 3 | | | |
| `toyota-corolla.jpg` | Toyota Corolla | | | |
| `toyota-yaris.jpg` | Toyota Yaris | | | |
| `volkswagen-golf.jpg` | Volkswagen Golf | | | |

## Datos y otras fuentes

- **Puntuaciones de seguridad** de la base de conocimiento de modelos
  (`db/migration/V3`–`V6`): ancladas a las valoraciones públicas de
  [Euro NCAP](https://www.euroncap.com).
- **Catálogo de marcas y modelos** del comparador de vehículos personalizados
  (`frontend/src/data/vehicleCatalog.json`): generado a partir de
  [Wikidata](https://www.wikidata.org) mediante consultas SPARQL
  (`frontend/scripts/fetch-vehicle-catalog.mjs`). Wikidata se publica bajo
  licencia [CC0](https://creativecommons.org/publicdomain/zero/1.0/) (dominio
  público).
