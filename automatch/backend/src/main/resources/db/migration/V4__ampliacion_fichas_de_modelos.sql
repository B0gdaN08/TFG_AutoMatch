-- ============================================================================
-- V4: Ampliacion de la base de conocimiento de modelos (73 fichas nuevas,
-- ~35 marcas en total con V3).
--
-- Metodologia de las valoraciones (orientativas, con fines academicos):
--  - safety: anclada a las estrellas Euro NCAP publicas de cada modelo
--    (escala coherente con V3: 0-1* = 5.0-5.5, 2* = 6.0, 3* = 6.5-7.0,
--    4* = 7.5-8.0, 5* = 8.5-9.5 segun protocolo y puntuacion).
--    Verificado contra euroncap.com: Suzuki Swift 2024 = 3*, Dacia Spring = 1*,
--    Renault Captur 2024 = 4*, MG3 = 4*, Fiat Panda = 0*.
--  - reliability: reputacion de fiabilidad por marca/modelo segun informes
--    publicos (tipo TUV Report / encuestas de fiabilidad): Toyota/Lexus/Honda
--    altas, coreanas medio-altas, grupo VW medias, Stellantis medio-bajas.
--  - equipment: dotacion tipica del acabado medio del segmento.
--  - maintenance_per_year / depreciation5y: patrones de coste y valor residual
--    por marca y segmento del mercado español.
--
-- Los coches que no coincidan con ninguna ficha reciben la media de su marca
-- o la media global (ver ModelScoreService); el admin puede corregir valores
-- durante la moderacion.
-- ============================================================================

INSERT INTO model_scores (brand, model, model_year, reliability, safety, equipment, maintenance_per_year, depreciation5y) VALUES
-- Grupo Volkswagen
('Volkswagen', 'Polo',                2025, 7.5, 8.5, 7.0, 380, 0.47),
('Volkswagen', 'T-Roc',               2025, 7.5, 8.5, 7.5, 420, 0.46),
('Volkswagen', 'Tiguan',              2025, 7.5, 9.0, 8.5, 470, 0.47),
('Volkswagen', 'ID.3',                2025, 7.0, 8.5, 8.0, 300, 0.55),
('Volkswagen', 'ID.4',                2025, 7.0, 8.5, 8.5, 320, 0.55),
('Seat',       'Arona',               2025, 7.5, 8.0, 7.0, 360, 0.48),
('Seat',       'Ateca',               2024, 7.5, 8.0, 7.5, 400, 0.49),
('Cupra',      'Born',                2025, 7.0, 8.5, 8.5, 300, 0.53),
('Cupra',      'León',                2025, 7.0, 8.5, 8.5, 450, 0.51),
('Skoda',      'Fabia',               2025, 8.0, 8.5, 7.0, 350, 0.45),
('Skoda',      'Kamiq',               2025, 8.0, 8.5, 7.5, 380, 0.46),
('Skoda',      'Karoq',               2025, 8.0, 8.0, 7.5, 410, 0.46),
('Skoda',      'Enyaq',               2025, 7.5, 9.0, 8.5, 310, 0.50),
('Audi',       'A1',                  2024, 7.5, 8.5, 8.0, 520, 0.48),
('Audi',       'Q2',                  2024, 7.5, 8.0, 8.0, 530, 0.48),
('Audi',       'Q3',                  2025, 7.5, 8.5, 8.5, 560, 0.47),
-- Grupo Renault y Dacia
('Renault',    'Captur',              2025, 7.0, 7.5, 7.0, 370, 0.49),
('Renault',    'Austral',             2025, 7.0, 8.5, 8.0, 420, 0.50),
('Renault',    'Megane E-Tech',       2025, 7.0, 8.5, 8.0, 290, 0.55),
('Dacia',      'Spring',              2025, 7.0, 5.5, 5.0, 260, 0.50),
('Dacia',      'Bigster',             2025, 7.5, 6.5, 6.5, 380, 0.42),
-- Grupo Stellantis
('Peugeot',    '2008',                2025, 6.5, 7.5, 7.5, 390, 0.51),
('Peugeot',    '308',                 2025, 6.5, 7.5, 7.5, 400, 0.51),
('Peugeot',    '5008',                2025, 6.8, 8.0, 8.0, 460, 0.51),
('Citroen',    'C4',                  2025, 6.5, 7.5, 7.0, 380, 0.53),
('Citroen',    'C3 Aircross',         2025, 6.5, 7.0, 6.5, 370, 0.51),
('Citroen',    'C5 Aircross',         2025, 6.5, 7.5, 7.5, 430, 0.53),
('Opel',       'Astra',               2025, 7.0, 7.5, 7.5, 400, 0.52),
('Opel',       'Mokka',               2025, 6.8, 7.5, 7.0, 380, 0.52),
('Fiat',       'Panda',               2024, 6.5, 5.0, 5.0, 300, 0.48),
('Fiat',       '500e',                2025, 6.5, 7.5, 7.0, 280, 0.58),
('Fiat',       '600',                 2025, 6.5, 7.5, 7.0, 350, 0.54),
('Jeep',       'Avenger',             2025, 6.5, 6.5, 7.0, 400, 0.52),
-- Japonesas
('Toyota',     'C-HR',                2025, 9.0, 8.5, 8.0, 330, 0.40),
('Toyota',     'RAV4',                2025, 9.0, 8.5, 8.0, 380, 0.40),
('Toyota',     'Aygo X',              2025, 9.0, 7.5, 6.0, 280, 0.42),
('Honda',      'Jazz',                2025, 9.0, 8.5, 7.0, 340, 0.42),
('Honda',      'Civic',               2025, 9.0, 8.5, 8.0, 380, 0.43),
('Honda',      'HR-V',                2025, 9.0, 7.5, 7.5, 370, 0.43),
('Mazda',      'Mazda2',              2024, 8.5, 7.0, 6.5, 320, 0.46),
('Mazda',      'CX-5',                2024, 8.5, 8.0, 7.5, 400, 0.45),
('Suzuki',     'Swift',               2025, 8.5, 6.5, 6.5, 300, 0.46),
('Suzuki',     'Vitara',              2024, 8.5, 7.0, 6.5, 330, 0.47),
('Nissan',     'Juke',                2025, 7.5, 8.0, 7.0, 390, 0.49),
('Lexus',      'NX',                  2025, 9.5, 9.0, 8.5, 550, 0.42),
-- Coreanas
('Kia',        'Picanto',             2025, 8.5, 6.5, 6.0, 290, 0.45),
('Kia',        'Ceed',                2025, 8.0, 8.0, 7.5, 370, 0.46),
('Kia',        'Niro',                2025, 8.5, 8.0, 8.0, 350, 0.44),
('Kia',        'EV6',                 2025, 8.0, 8.5, 9.0, 300, 0.50),
('Hyundai',    'i10',                 2025, 8.5, 6.5, 6.0, 290, 0.46),
('Hyundai',    'i20',                 2025, 8.5, 7.5, 6.5, 320, 0.47),
('Hyundai',    'i30',                 2024, 8.0, 8.0, 7.0, 360, 0.47),
('Hyundai',    'Kona',                2025, 8.0, 7.5, 7.5, 380, 0.45),
('Hyundai',    'Ioniq 5',             2025, 8.0, 8.5, 9.0, 300, 0.50),
-- Premium europeas
('BMW',        'X1',                  2025, 7.5, 9.0, 8.5, 580, 0.47),
('BMW',        'Serie 3',             2025, 7.5, 9.0, 9.0, 600, 0.48),
('Mercedes',   'Clase A',             2024, 7.5, 9.0, 8.5, 580, 0.48),
('Mercedes',   'GLA',                 2025, 7.5, 9.0, 8.5, 600, 0.47),
('Mercedes',   'Clase C',             2025, 7.5, 9.0, 9.0, 640, 0.48),
('Volvo',      'XC40',                2025, 8.0, 9.0, 8.5, 520, 0.46),
('Volvo',      'EX30',                2025, 7.5, 9.0, 8.5, 350, 0.52),
('Mini',       'Cooper',              2025, 7.0, 7.5, 8.0, 480, 0.46),
('Land Rover', 'Range Rover Evoque',  2024, 5.5, 8.5, 8.5, 650, 0.55),
-- Ford
('Ford',       'Focus',               2024, 7.0, 8.5, 7.5, 400, 0.52),
('Ford',       'Kuga',                2025, 7.0, 8.5, 7.5, 430, 0.50),
-- Electricos y asiaticas recientes
('Tesla',      'Model Y',             2025, 7.0, 9.5, 9.0, 260, 0.50),
('MG',         'MG3',                 2025, 7.0, 7.5, 7.0, 300, 0.52),
('MG',         'ZS',                  2025, 6.8, 7.5, 7.0, 320, 0.54),
('MG',         'HS',                  2025, 6.8, 8.0, 7.5, 340, 0.54),
('BYD',        'Dolphin',             2025, 7.0, 8.5, 8.0, 280, 0.57),
('BYD',        'Atto 3',              2025, 7.0, 8.5, 8.0, 290, 0.57),
('BYD',        'Seal',                2025, 7.0, 9.0, 8.5, 300, 0.56),
('Omoda',      'Omoda 5',             2025, 6.5, 8.0, 7.5, 350, 0.58);
