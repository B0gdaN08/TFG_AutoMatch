# AutoMatch 🚗

[![CI](https://github.com/B0gdaN08/TFG_AutoMatch/actions/workflows/ci.yml/badge.svg)](https://github.com/B0gdaN08/TFG_AutoMatch/actions/workflows/ci.yml)

**Plataforma web de apoyo a la decisión de compra de coches** (TFG · Ingeniería Informática · URJC).

AutoMatch ayuda a responder la pregunta "¿qué coche me compro?" con datos: catálogo filtrable del mercado español, comparador cara a cara, calculadora de **coste total de propiedad (TCO)** y un **recomendador multicriterio** que ordena los coches según las prioridades de cada usuario.

---

## Funcionalidades

- **Catálogo** con búsqueda, filtros (tipo, combustible, etiqueta DGT, precio mínimo/máximo), ordenación y paginación sobre 28 modelos reales del mercado español con datos orientativos 2025-2026.
- **Ficha de coche** con especificaciones, puntuaciones (fiabilidad, seguridad, equipamiento) y resumen de coste real.
- **Comparador** de 2-3 coches con el mejor valor de cada fila resaltado.
- **Calculadora TCO**: depreciación + combustible/energía + seguro + mantenimiento + impuestos, configurable por años y km/año.
- **Recomendador multicriterio** con dos algoritmos a elegir: **Suma Ponderada** (normalización min-max) y **TOPSIS** (Hwang y Yoon, 1981). El usuario pondera 7 criterios de 0 a 5 y obtiene un ranking explicable.
- **Usuarios** con registro/login (JWT + BCrypt), **favoritos** y **anuncios propios** con moderación.
- **API de administración** (rol ADMIN) para crear, editar, moderar y borrar coches.

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Spring Boot 3.4 · Java 21 · Spring Security (JWT) · Spring Data JPA |
| Base de datos | H2 en fichero (desarrollo) · PostgreSQL 16 (producción) · migraciones con **Flyway** |
| Frontend | React 18 · Vite · React Router · CSS propio (sin frameworks) |
| Despliegue | Docker multi-stage + nginx + docker compose |

```
automatch/
├── docker-compose.yml        # db (PostgreSQL) + backend + frontend
├── .env.example              # variables de entorno documentadas
├── backend/                  # API REST (puerto 8080)
│   ├── Dockerfile            # build multi-stage: Maven → JRE 21 alpine
│   └── src/main/
│       ├── java/es/urjc/automatch/
│       │   ├── model/            # Entidades JPA (Car, User, Favorite)
│       │   ├── repository/       # Spring Data + Specifications
│       │   ├── service/          # Lógica: TCO, recomendador, auth, costes
│       │   ├── controller/       # Endpoints REST
│       │   ├── security/         # JWT (filtro + servicio)
│       │   └── config/           # Seguridad, CORS y datos de ejemplo
│       └── resources/
│           ├── application.properties        # común (defecto: perfil dev)
│           ├── application-dev.properties    # H2 + consola + Swagger
│           ├── application-prod.properties   # PostgreSQL + endurecido
│           └── db/migration/                 # V1 esquema, V2 usuarios (Flyway)
└── frontend/                 # SPA React (puerto 5173)
    ├── Dockerfile            # build de Vite servido con nginx (proxy /api)
    └── src/
        ├── pages/            # Catálogo, ficha, comparador, recomendador, TCO…
        ├── components/       # Navbar, tarjetas, barras de puntuación
        └── api.js            # Cliente HTTP con el token JWT
```

## Arranque en desarrollo (igual que siempre)

Requisitos: **Java 21**, **Maven 3.9+** y **Node 18+**.

**1. Backend** (en una terminal):

```bash
cd backend
mvn spring-boot:run
```

Arranca con el perfil `dev` por defecto: H2 en fichero (`backend/data/`), consola H2 y Swagger abiertos. Flyway crea el esquema y los usuarios; el catálogo de ejemplo lo siembra `DataLoader` la primera vez.

> **Nota (BDs anteriores a Flyway):** si tu `backend/data/` es de una versión anterior, Flyway lo adopta automáticamente (`baseline-on-migrate`). Si algo no cuadra al validar el esquema, borra la carpeta `backend/data/` y arranca de nuevo: se regenera con los datos de ejemplo.

**2. Frontend** (en otra terminal):

```bash
cd frontend
npm install
npm run dev
```

Abre **http://localhost:5173**.

### Cuentas de prueba

| Email | Contraseña | Rol |
|-------|-----------|-----|
| demo@automatch.es | demo123 | Usuario |
| admin@automatch.es | admin123 | Admin (moderación y CRUD de coches) |

### Consola de la base de datos (solo dev)

Con el backend arrancado: http://localhost:8080/h2-console
JDBC URL `jdbc:h2:file:./data/automatch` · usuario `sa` · sin contraseña.

### Documentación de la API (solo dev)

Swagger UI: http://localhost:8080/swagger-ui.html (desactivado en producción).

## Arranque con Docker (producción local)

Requisitos: **Docker** con el plugin compose.

```bash
cp .env.example .env          # y define APP_JWT_SECRET (p. ej.: openssl rand -base64 48)
docker compose up --build
```

Levanta tres servicios:

| Servicio | Imagen | Puerto | Notas |
|----------|--------|--------|-------|
| `db` | postgres:16 | — | volumen `db-data`, healthcheck con `pg_isready` |
| `backend` | build multi-stage (Maven → JRE 21) | 8080 | perfil `prod`, espera a que `db` esté sana, volumen `uploads` |
| `frontend` | build de Vite + nginx | **5173** | nginx sirve la SPA y hace proxy de `/api`, `/uploads` e `/images` |

Abre **http://localhost:5173** (misma URL que en desarrollo). Al arrancar, el backend registra en el log el perfil activo (`The following 1 profile is active: "prod"`) y las migraciones Flyway aplicadas.

## Variables de entorno

Documentadas también en [.env.example](.env.example) y [frontend/.env.example](frontend/.env.example).

### Backend

| Variable | Obligatoria en prod | Descripción |
|----------|:---:|-------------|
| `SPRING_PROFILES_ACTIVE` | sí (`prod`) | Perfil. Sin definir: `dev` |
| `SPRING_DATASOURCE_URL` | sí | `jdbc:postgresql://host:5432/automatch` |
| `SPRING_DATASOURCE_USERNAME` | sí | Usuario de la BD |
| `SPRING_DATASOURCE_PASSWORD` | sí | Contraseña de la BD |
| `APP_JWT_SECRET` | **sí, sin default** | Clave de firma JWT (≥ 32 caracteres). En prod, si falta, el backend no arranca |
| `APP_CORS_ORIGINS` | no | Orígenes permitidos separados por comas (defecto `http://localhost:5173`) |
| `APP_SEED_ADMIN_PASSWORD` / `APP_SEED_DEMO_PASSWORD` | no | Solo se usan si la BD no tiene usuarios |

### Frontend (en build)

| Variable | Descripción |
|----------|-------------|
| `VITE_API_URL` | URL del backend. Sin definir: `http://localhost:8080` (dev). Vacía: rutas relativas (detrás del proxy nginx) |

## Migraciones (Flyway)

- `V1__esquema.sql` — tablas `users`, `cars`, `car_images`, `favorites` con constraints e índices (`cars.status`, `cars.owner_id`, `cars.price`). SQL compatible con H2 y PostgreSQL.
- `V2__datos_iniciales.sql` — usuarios de demostración (inserciones idempotentes).

Hibernate arranca en modo `validate` en todos los perfiles: **Flyway es la única fuente del esquema**. Los tests (H2 en memoria) también ejecutan las migraciones, así que cualquier desajuste entre el SQL y las entidades rompe la suite.

## Guía corta de despliegue

### Backend + PostgreSQL en Render o Railway

1. Crea una base de datos PostgreSQL gestionada y copia su URL/credenciales.
2. Crea un servicio web desde el repo apuntando a `backend/` (ambas plataformas detectan el `Dockerfile`).
3. Define las variables: `SPRING_PROFILES_ACTIVE=prod`, `SPRING_DATASOURCE_URL` (formato `jdbc:postgresql://…`), `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_JWT_SECRET` y `APP_CORS_ORIGINS` con la URL pública del frontend.
4. Al primer arranque Flyway crea el esquema y `DataLoader` siembra el catálogo.

> Las fotos subidas van al sistema de ficheros (`/app/uploads`): en Render usa un disco persistente; en Railway, un volumen.

### Frontend en Vercel (o Netlify)

1. Importa el repo con raíz `frontend/`, build `npm run build`, salida `dist/`.
2. Define `VITE_API_URL=https://tu-backend.onrender.com` (la URL pública del backend).
3. Añade la URL del frontend a `APP_CORS_ORIGINS` del backend.

### Todo junto en un VPS

El propio `docker-compose.yml` sirve tal cual: clona el repo, crea `.env` con un `APP_JWT_SECRET` fuerte y una `POSTGRES_PASSWORD` propia, y ejecuta `docker compose up --build -d`. Pon delante un reverse proxy con TLS (Caddy o nginx) apuntando al puerto 5173.

## Endpoints principales

```
POST   /api/auth/register            Registro (devuelve JWT)
POST   /api/auth/login               Login (devuelve JWT)
GET    /api/cars                     Catálogo paginado (?search&type&fuel&minPrice&maxPrice&dgtLabel&page&size&sort)
GET    /api/cars/all                 Lista completa (selects de comparador/TCO)
GET    /api/cars/{id}                Ficha de un coche
GET    /api/cars/compare?ids=1,2,3   Comparador (2-3 coches)
GET    /api/tco/{carId}?years&km     Coste total de propiedad
POST   /api/recommendations          Ranking multicriterio (pesos + filtros + method)
GET    /api/favorites                Favoritos del usuario   [auth]
POST   /api/favorites/{carId}        Añadir favorito          [auth]
DELETE /api/favorites/{carId}        Quitar favorito          [auth]
POST   /api/cars                     Publicar anuncio         [auth]
PUT    /api/cars/{id}/approve        Aprobar anuncio          [ADMIN]
PUT    /api/cars/{id}                Editar coche             [ADMIN]
DELETE /api/cars/{id}                Borrar coche             [dueño o ADMIN]
```

## Dónde tocar cada cosa

- **Añadir/editar coches de ejemplo** → `backend/.../config/DataLoader.java` (o por API con el usuario admin). Si cambias el seed, borra `backend/data/` para regenerarla.
- **Esquema de la BD** → nueva migración `V3__...sql` en `backend/src/main/resources/db/migration/` (no editar las aplicadas).
- **Precios de carburante, seguro e impuestos** → `backend/.../service/CarCosts.java`.
- **Fórmula del TCO** → `backend/.../service/TcoService.java`.
- **Algoritmos del recomendador** → `backend/.../service/ranking/` (`WeightedSumAlgorithm`, `TopsisAlgorithm`).
- **Colores y estilos** → variables en `frontend/src/styles.css` (`:root`).
- **Configuración por perfil** → `backend/src/main/resources/application-{dev,prod}.properties`.

## Roadmap (ideas para iterar y para la memoria)

1. ~~Implementar TOPSIS y compararlo con la suma ponderada~~ ✅ (ver `docs/experimento-recomendador.md`)
2. ~~Migrar a PostgreSQL + Docker Compose~~ ✅
3. ~~Tests unitarios y de integración con cobertura~~ ✅ (JaCoCo + Vitest)
4. ~~Filtros con JPA Specifications + paginación~~ ✅
5. **Panel de administración** en el frontend (el backend ya está listo).
6. Importar precios reales (API/CSV) y gráficas de evolución.
7. Guardar y compartir comparativas y resultados del recomendador.
8. Despliegue en la nube (Render/Railway + Vercel) para la demo del tribunal.

---

*Los datos del catálogo y las fórmulas de coste son estimaciones orientativas con fines académicos.*
