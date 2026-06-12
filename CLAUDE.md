# AutoMatch — convenciones del proyecto

## Estructura del repo
- Backend (Spring Boot): `automatch/backend`
- Frontend (React + Vite): `automatch/frontend`

## Stack
- Backend: Spring Boot 3.4, Java 21, Spring Security con JWT (jjwt 0.12), JPA, base de datos H2 en desarrollo.
- Frontend: React 18 + Vite + react-router. JSX plano (sin TypeScript). CSS propio en `src/styles.css` (sin Tailwind ni otros frameworks de estilos).

## Estilo de código

### Backend
- Sin Lombok: getters/setters explícitos en los modelos/DTOs.
- La lógica vive en los servicios; los controladores son finos (validan, delegan y devuelven).
- Los errores se modelan como excepciones y se gestionan de forma centralizada en `GlobalExceptionHandler`.
- Los mensajes de error de cara al usuario van en español.

### Frontend
- JSX plano, sin TypeScript.
- Componentes reutilizables en `src/components`, páginas en `src/pages`.
- Un único cliente HTTP en `src/api.js` para hablar con el backend.
- Los textos de la interfaz van en español.

## Reglas
- Todo cambio nuevo en el backend debe incluir tests.
- No romper endpoints existentes sin avisarlo antes.
- Antes de dar por terminada una tarea, ejecutar:
  - `mvn -q test` en `automatch/backend`
  - `npm run build` en `automatch/frontend`

## Cómo arrancar el proyecto
- Backend: `cd automatch/backend && mvn spring-boot:run`
- Frontend: `cd automatch/frontend && npm run dev`

### Usuarios de demo
- Admin: `admin@automatch.es` / `admin123`
- Usuario: `demo@automatch.es` / `demo123`
