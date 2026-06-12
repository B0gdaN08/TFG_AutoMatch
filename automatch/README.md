# AutoMatch 🚗

**Plataforma web de apoyo a la decisión de compra de coches** (TFG · Ingeniería Informática · URJC).

AutoMatch ayuda a responder la pregunta "¿qué coche me compro?" con datos: catálogo filtrable del mercado español, comparador cara a cara, calculadora de **coste total de propiedad (TCO)** y un **recomendador multicriterio** que ordena los coches según las prioridades de cada usuario.

---

## Funcionalidades

- **Catálogo** con búsqueda y filtros (tipo, combustible, precio máximo) sobre 28 modelos reales del mercado español con datos orientativos 2025-2026.
- **Ficha de coche** con especificaciones, puntuaciones (fiabilidad, seguridad, equipamiento) y resumen de coste real.
- **Comparador** de 2-3 coches con el mejor valor de cada fila resaltado.
- **Calculadora TCO**: depreciación + combustible/energía + seguro + mantenimiento + impuestos, configurable por años y km/año.
- **Recomendador multicriterio** (suma ponderada con normalización min-max): el usuario pondera 7 criterios de 0 a 5 y obtiene un ranking explicable, con el detalle de cada criterio.
- **Usuarios** con registro/login (JWT + BCrypt) y **favoritos**.
- **API de administración** (rol ADMIN) para crear, editar y borrar coches.

## Stack

| Capa | Tecnología |
|------|------------|
| Backend | Spring Boot 3.4 · Java 21 · Spring Security (JWT) · Spring Data JPA |
| Base de datos | H2 en fichero (cero instalación; migrable a PostgreSQL) |
| Frontend | React 18 · Vite · React Router · CSS propio (sin frameworks) |

Arquitectura sencilla a propósito: **un** backend y **un** frontend, sin Docker ni servicios externos, para que sea fácil de ejecutar, entender y modificar.

```
automatch/
├── backend/                  # API REST (puerto 8080)
│   └── src/main/java/es/urjc/automatch/
│       ├── model/            # Entidades JPA (Car, User, Favorite)
│       ├── repository/       # Spring Data
│       ├── service/          # Lógica: TCO, recomendador, auth, costes
│       ├── controller/       # Endpoints REST
│       ├── security/         # JWT (filtro + servicio)
│       └── config/           # Seguridad, CORS y datos de ejemplo
└── frontend/                 # SPA React (puerto 5173)
    └── src/
        ├── pages/            # Catálogo, ficha, comparador, recomendador, TCO…
        ├── components/       # Navbar, tarjetas, barras de puntuación
        └── api.js            # Cliente HTTP con el token JWT
```

## Cómo ejecutarlo

Requisitos: **Java 21**, **Maven 3.9+** y **Node 18+**. (Si usas IntelliJ o VS Code con extensiones de Java, Maven viene gestionado por el IDE.)

**1. Backend** (en una terminal):

```bash
cd backend
mvn spring-boot:run
```

La primera vez descarga dependencias y crea la base de datos en `backend/data/` con los coches y usuarios de ejemplo.

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
| admin@automatch.es | admin123 | Admin (CRUD de coches por API) |

### Consola de la base de datos

Con el backend arrancado: http://localhost:8080/h2-console
JDBC URL `jdbc:h2:file:./data/automatch` · usuario `sa` · sin contraseña.

## Endpoints principales

```
POST   /api/auth/register            Registro (devuelve JWT)
POST   /api/auth/login               Login (devuelve JWT)
GET    /api/cars                     Catálogo (?search&type&fuel&maxPrice)
GET    /api/cars/{id}                Ficha de un coche
GET    /api/cars/compare?ids=1,2,3   Comparador (2-3 coches)
GET    /api/tco/{carId}?years&km     Coste total de propiedad
POST   /api/recommendations          Ranking multicriterio (pesos + filtros)
GET    /api/favorites                Favoritos del usuario   [auth]
POST   /api/favorites/{carId}        Añadir favorito          [auth]
DELETE /api/favorites/{carId}        Quitar favorito          [auth]
POST   /api/cars                     Crear coche              [ADMIN]
PUT    /api/cars/{id}                Editar coche             [ADMIN]
DELETE /api/cars/{id}                Borrar coche             [ADMIN]
```

## Dónde tocar cada cosa

- **Añadir/editar coches de ejemplo** → `backend/.../config/DataLoader.java` (o por API con el usuario admin). Si cambias el seed, borra la carpeta `backend/data/` para regenerarla.
- **Precios de carburante, seguro e impuestos** → `backend/.../service/CarCosts.java` (constantes documentadas).
- **Fórmula del TCO** → `backend/.../service/TcoService.java`.
- **Criterios y pesos del recomendador** → `backend/.../service/RecommendationService.java` y etiquetas en `frontend/src/pages/Recomendador.jsx`.
- **Colores y estilos** → variables en `frontend/src/styles.css` (`:root`).
- **Clave JWT y configuración** → `backend/src/main/resources/application.properties`.

## Roadmap (ideas para iterar y para la memoria)

1. Implementar **TOPSIS** y compararlo con la suma ponderada (capítulo de algoritmos).
2. Migrar a **PostgreSQL + Docker Compose** (capítulo de despliegue).
3. **Tests** unitarios y de integración (JUnit + MockMvc) con cobertura.
4. **Panel de administración** en el frontend (el backend ya está listo).
5. Filtros con **JPA Specifications** en lugar de filtrado en memoria.
6. Importar precios reales (API/CSV) y gráficas de evolución.
7. Guardar y compartir comparativas y resultados del recomendador.
8. Despliegue en la nube (Render/Railway + Vercel) para la demo del tribunal.

---

*Los datos del catálogo y las fórmulas de coste son estimaciones orientativas con fines académicos.*
