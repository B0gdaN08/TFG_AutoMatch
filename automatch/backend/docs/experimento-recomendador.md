# Experimento comparativo: Suma Ponderada vs TOPSIS

Catálogo: 28 coches del mercado español (semilla DataLoader, perfil test H2).
Todos los cálculos son deterministas y reproducibles con `mvn verify`.

## 1. Rankings por perfil

### Perfil: Ahorrador

Pesos: price=5, efficiency=5, reliability=2, safety=1, space=1, performance=1, equipment=1

| # | WSM — Suma Ponderada | Score | TOPSIS | Score |
|---|---------------------|-------|--------|-------|
| 1 | Renault 5 E-Tech | 61.2 | Renault 5 E-Tech | 69.4 |
| 2 | Kia EV3 | 58.7 | MG MG4 | 65.8 |
| 3 | Tesla Model 3 | 57.5 | Kia EV3 | 56.9 |

**Correlación de Spearman:** ρ = 0,883  
**Cambio medio de posición por coche:** 2,9 puestos

### Perfil: Familiar

Pesos: price=2, efficiency=2, reliability=4, safety=5, space=5, performance=1, equipment=3

| # | WSM — Suma Ponderada | Score | TOPSIS | Score |
|---|---------------------|-------|--------|-------|
| 1 | Tesla Model 3 | 64.9 | Skoda Kodiaq TDI | 72.3 |
| 2 | Skoda Kodiaq TDI | 61.2 | Tesla Model 3 | 54.3 |
| 3 | Hyundai Tucson HEV | 58.6 | Dacia Jogger | 53.5 |

**Correlación de Spearman:** ρ = 0,743  
**Cambio medio de posición por coche:** 4,1 puestos

### Perfil: Deportivo

Pesos: price=1, efficiency=1, reliability=3, safety=3, space=1, performance=5, equipment=3

| # | WSM — Suma Ponderada | Score | TOPSIS | Score |
|---|---------------------|-------|--------|-------|
| 1 | Tesla Model 3 | 76.9 | Tesla Model 3 | 81.9 |
| 2 | Hyundai Tucson HEV | 63.4 | Hyundai Tucson HEV | 63.0 |
| 3 | Kia Sportage HEV | 63.2 | Kia Sportage HEV | 62.8 |

**Correlación de Spearman:** ρ = 0,881  
**Cambio medio de posición por coche:** 2,8 puestos

### Perfil: Equilibrado

Pesos: price=3, efficiency=3, reliability=3, safety=3, space=3, performance=3, equipment=3

| # | WSM — Suma Ponderada | Score | TOPSIS | Score |
|---|---------------------|-------|--------|-------|
| 1 | Tesla Model 3 | 67.7 | Tesla Model 3 | 61.9 |
| 2 | Kia EV3 | 59.0 | Kia EV3 | 50.7 |
| 3 | Hyundai Tucson HEV | 53.8 | Skoda Kodiaq TDI | 50.6 |

**Correlación de Spearman:** ρ = 0,690  
**Cambio medio de posición por coche:** 4,6 puestos

### Perfil: Ecológico

Pesos: price=3, efficiency=5, reliability=3, safety=3, space=2, performance=2, equipment=3 · Filtro combustible: [ELECTRICO]

| # | WSM — Suma Ponderada | Score | TOPSIS | Score |
|---|---------------------|-------|--------|-------|
| 1 | Tesla Model 3 | 76.2 | Tesla Model 3 | 60.9 |
| 2 | Kia EV3 | 47.9 | Kia EV3 | 46.1 |
| 3 | Renault 5 E-Tech | 31.0 | Renault 5 E-Tech | 39.4 |

**Correlación de Spearman:** ρ = 1,000  
**Cambio medio de posición por coche:** 0,0 puestos

## 2. Análisis de sensibilidad — perfil Equilibrado, peso `price` de 0 a 5

El resto de pesos se mantienen a 3. Se registra el primer clasificado de cada método.

| Peso `price` | #1 WSM | #1 TOPSIS |
|:------------:|--------|----------|
| 0 | Tesla Model 3 | Tesla Model 3 |
| 1 | Tesla Model 3 | Tesla Model 3 |
| 2 | Tesla Model 3 | Tesla Model 3 |
| 3 | Tesla Model 3 | Tesla Model 3 |
| 4 | Tesla Model 3 | Tesla Model 3 |
| 5 | Tesla Model 3 | Tesla Model 3 |

## 3. Análisis de rank reversal — perfil Equilibrado

Se elimina una alternativa del conjunto y se recalcula el ranking. Se cuentan los pares de coches cuyo orden relativo se invierte respecto al ranking base (crítica de Belton y Gear, 1983, a TOPSIS: la normalización vectorial y las soluciones ideales dependen del conjunto de alternativas; en WSM la normalización min-max solo cambia si el coche eliminado era un extremo de algún criterio).

| Método | Alternativa eliminada | Pares invertidos |
|--------|----------------------|:----------------:|
| WSM — Suma Ponderada | Kia EV3 (puesto 2 de 28) | 0 |
| WSM — Suma Ponderada | Citroen C3 (puesto 28 de 28) | 0 |
| TOPSIS | Kia EV3 (puesto 2 de 28) | 0 |
| TOPSIS | BMW Serie 1 118 (puesto 28 de 28) | 1 |

