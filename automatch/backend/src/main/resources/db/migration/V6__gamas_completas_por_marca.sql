-- ============================================================================
-- V6: Tercera ampliacion — gamas completas por marca (~78 fichas nuevas,
-- ~300 modelos en total). Misma metodologia que V4/V5 (safety anclada a
-- Euro NCAP, resto por reputacion de marca y segmento).
--
-- Enfasis en dos frentes:
--  1. Gamas premium y electricas completas (A7/A8/Q7/Q8, Serie 2/4/7, X4/X6,
--     i5, Clase S/GLE/EQB/EQE, ID.5/ID.Buzz, EV4, Ioniq...).
--  2. Modelos descatalogados muy frecuentes en el mercado de ocasion español
--     (Zoe, Kadjar, Mondeo, EcoSport, Punto, 500, Giulietta, ix35, V40,
--     Logan, C1, Mii, up!, ForTwo...), que es lo que mas publican los
--     vendedores particulares.
-- ============================================================================

INSERT INTO model_scores (brand, model, model_year, reliability, safety, equipment, maintenance_per_year, depreciation5y) VALUES
-- Grupo Volkswagen
('Volkswagen', 'Arteon',           2023, 7.5, 8.5, 8.5, 500, 0.52),
('Volkswagen', 'ID.5',             2025, 7.0, 8.5, 8.5, 330, 0.55),
('Volkswagen', 'ID. Buzz',         2025, 7.0, 8.5, 8.5, 380, 0.50),
('Volkswagen', 'Caddy',            2024, 7.5, 8.0, 6.5, 400, 0.44),
('Volkswagen', 'up!',              2022, 7.5, 6.5, 5.5, 290, 0.45),
('Seat',       'Alhambra',         2020, 7.5, 7.5, 7.5, 450, 0.48),
('Seat',       'Mii',              2021, 7.5, 6.5, 5.5, 280, 0.45),
('Cupra',      'Ateca',            2024, 7.0, 8.0, 8.0, 460, 0.50),
('Cupra',      'Raval',            2026, 7.0, 8.0, 8.0, 380, 0.52),
('Skoda',      'Citigo',           2020, 7.5, 6.5, 5.5, 280, 0.45),
('Audi',       'A7',               2024, 7.5, 8.5, 9.0, 680, 0.52),
('Audi',       'A8',               2024, 7.5, 8.5, 9.5, 750, 0.56),
('Audi',       'Q7',               2024, 7.0, 8.5, 9.0, 700, 0.52),
('Audi',       'Q8',               2024, 7.0, 8.5, 9.0, 720, 0.52),
('Audi',       'TT',               2022, 7.5, 7.5, 8.0, 550, 0.45),
-- BMW y Mercedes
('BMW',        'Serie 2',          2025, 7.5, 8.5, 8.5, 560, 0.48),
('BMW',        'Serie 4',          2025, 7.5, 8.5, 9.0, 620, 0.48),
('BMW',        'Serie 7',          2024, 7.0, 9.0, 9.5, 780, 0.57),
('BMW',        'X4',               2024, 7.5, 8.5, 8.5, 640, 0.48),
('BMW',        'X6',               2024, 7.0, 8.5, 9.0, 700, 0.51),
('BMW',        'i5',               2025, 7.0, 9.0, 9.0, 450, 0.54),
('Mercedes',   'Clase S',          2024, 7.5, 9.5, 9.5, 800, 0.55),
('Mercedes',   'GLE',              2024, 7.0, 9.0, 9.0, 700, 0.50),
('Mercedes',   'EQB',              2025, 7.0, 8.5, 8.5, 400, 0.56),
('Mercedes',   'EQE',              2025, 7.0, 9.0, 9.0, 450, 0.58),
('Mercedes',   'Clase V',          2024, 7.0, 8.0, 8.0, 620, 0.48),
-- Grupo Renault y Dacia
('Renault',    'Kadjar',           2021, 7.0, 8.0, 7.0, 400, 0.52),
('Renault',    'Zoe',              2021, 7.0, 6.0, 6.5, 280, 0.58),
('Renault',    '4 E-Tech',         2025, 7.0, 8.0, 7.5, 300, 0.52),
('Renault',    'Kangoo',           2024, 7.0, 7.5, 6.0, 380, 0.45),
('Dacia',      'Logan',            2021, 7.5, 6.0, 5.0, 300, 0.44),
('Dacia',      'Lodgy',            2021, 7.0, 5.5, 5.0, 330, 0.45),
-- Grupo Stellantis
('Citroen',    'C1',               2021, 7.0, 6.5, 5.5, 280, 0.47),
('Citroen',    'C5 X',             2024, 6.5, 8.0, 8.0, 430, 0.55),
('DS',         'DS 3',             2024, 6.5, 7.5, 7.5, 440, 0.55),
('Opel',       'Zafira',           2020, 7.0, 7.0, 7.0, 420, 0.52),
('Fiat',       '500',              2023, 6.5, 6.5, 6.0, 300, 0.48),
('Fiat',       '500X',             2022, 6.5, 7.0, 6.5, 360, 0.53),
('Fiat',       'Punto',            2017, 6.5, 5.5, 5.0, 300, 0.50),
('Abarth',     '595',              2023, 6.5, 6.5, 7.0, 430, 0.45),
('Abarth',     '600e',             2025, 6.5, 7.5, 8.0, 430, 0.58),
('Alfa Romeo', 'Giulietta',        2020, 6.5, 7.0, 7.0, 460, 0.55),
-- Japonesas
('Toyota',     'Aygo',             2021, 9.0, 6.5, 5.5, 270, 0.42),
('Toyota',     'Corolla',          2025, 9.5, 9.0, 7.5, 310, 0.40),
('Toyota',     'Corolla Cross',    2025, 9.0, 8.5, 7.5, 340, 0.41),
('Mazda',      'CX-3',             2021, 8.5, 7.5, 7.0, 350, 0.47),
('Mazda',      'CX-80',            2025, 8.0, 8.5, 8.5, 520, 0.50),
('Suzuki',     'Across',           2024, 8.5, 8.0, 7.5, 400, 0.47),
('Mitsubishi', 'Outlander',        2025, 7.5, 8.5, 8.0, 450, 0.48),
('Mitsubishi', 'Eclipse Cross',    2023, 7.5, 7.5, 7.0, 400, 0.51),
('Subaru',     'Outback',          2024, 8.5, 8.5, 7.5, 470, 0.47),
('Lexus',      'ES',               2024, 9.5, 8.5, 8.5, 520, 0.45),
('Lexus',      'RZ',               2025, 9.0, 8.5, 8.5, 400, 0.52),
-- Coreanas
('Kia',        'EV4',              2025, 8.0, 8.5, 8.5, 310, 0.51),
('Hyundai',    'Ioniq',            2021, 8.5, 8.0, 7.0, 320, 0.48),
('Hyundai',    'ix35',             2015, 7.5, 6.5, 6.0, 380, 0.50),
-- Ford
('Ford',       'EcoSport',         2021, 6.8, 7.0, 6.5, 370, 0.53),
('Ford',       'Mondeo',           2021, 7.0, 8.0, 7.5, 430, 0.55),
-- Electricas, chinas y otros
('MG',         'Marvel R',         2024, 6.5, 7.5, 7.5, 340, 0.58),
('MG',         'S5',               2025, 6.8, 8.0, 7.5, 330, 0.55),
('BYD',        'Han',              2025, 7.0, 8.5, 8.5, 350, 0.58),
('BYD',        'Tang',             2025, 7.0, 8.5, 8.5, 380, 0.58),
('BYD',        'Dolphin Surf',     2025, 7.0, 8.0, 7.5, 260, 0.57),
('Volvo',      'EX40',             2025, 8.0, 9.0, 8.5, 380, 0.50),
('Volvo',      'V60',              2024, 8.0, 8.5, 8.0, 520, 0.49),
('Volvo',      'V40',              2019, 8.0, 7.5, 7.0, 450, 0.52),
('Polestar',   '3',                2025, 7.0, 9.0, 9.0, 420, 0.56),
('Smart',      'ForTwo',           2021, 7.0, 6.0, 6.0, 320, 0.50),
('Land Rover', 'Range Rover Sport',2024, 5.5, 8.5, 9.0, 750, 0.53),
('Land Rover', 'Range Rover Velar',2024, 5.5, 8.5, 8.5, 700, 0.56),
('Porsche',    'Cayenne',          2024, 8.0, 8.0, 9.0, 900, 0.42),
('Porsche',    'Taycan',           2024, 7.5, 8.5, 9.5, 700, 0.52),
('Jaecoo',     '5',                2025, 6.5, 8.0, 7.5, 380, 0.58),
('Omoda',      'Omoda 7',          2025, 6.5, 8.0, 8.0, 380, 0.58),
('Leapmotor',  'B10',              2025, 6.5, 8.0, 7.5, 300, 0.58),
('Xpeng',      'G9',               2025, 6.5, 8.5, 8.5, 380, 0.58),
('KGM',        'Tivoli',           2023, 6.5, 7.0, 6.5, 370, 0.55);
