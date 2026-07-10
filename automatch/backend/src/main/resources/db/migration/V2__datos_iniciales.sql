-- ============================================================================
-- V2: Usuarios de demostracion (idempotente: no duplica si ya existen,
-- necesario porque en BDs antiguas con baseline el DataLoader ya los creo).
-- Contrasenas por defecto: admin123 / demo123 (hash BCrypt).
-- En un despliegue real deben cambiarse tras el primer arranque.
-- El catalogo de coches lo siembra DataLoader solo si la tabla cars esta vacia.
-- ============================================================================

INSERT INTO users (email, password, name, role)
SELECT 'admin@automatch.es',
       '$2a$10$KRKxM8g6vFWU2kWscsycW.xKJvKJSzAAbUL8AF1h08OuVp7PWWYN2',
       'Admin',
       'ROLE_ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@automatch.es');

INSERT INTO users (email, password, name, role)
SELECT 'demo@automatch.es',
       '$2a$10$drcJRIdd5U8kJyx8SjMif..7JIU8lGvJC/B0pwnzl9bWwFPWRDRFC',
       'Usuario Demo',
       'ROLE_USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'demo@automatch.es');
