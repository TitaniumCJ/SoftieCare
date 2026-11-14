# Guia de datos de prueba — SoftieCare

Valores para probar y demostrar la app. Los rangos de cada nivel estan en el Blueprint, seccion 4.3 (tabla "Lectura de los cortes"); esta guia solo dice que capturar y que resultado esperar.

Niveles internos y lo que muestra la app: `NORMAL` = "ESTABLE" (verde), `WARNING` = "NO OPTIMO" (amarillo), `CRITICAL` = "CRITICO" (rojo). Excepcion: en `MOOD`, `CRITICAL` se muestra como "NO OPTIMO" (Blueprint 1.1).

Los datos son simulados y los cortes son heuristicos (Blueprint 1.1).

## 1. Captura manual por indicador

Cada fila es un valor para escribir en la captura guiada (`AssessmentScreen`) y el nivel que debe mostrar la app.

### BP_SYS — Presion sistolica (mmHg)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 120 | `NORMAL` |
| Precaucion baja | 92 | `WARNING` |
| Precaucion alta | 135 | `WARNING` |
| Malo bajo | 85 | `CRITICAL` |
| Malo alto (demo Fase 3) | 145 | `CRITICAL` |
| Fronteras | 94.99 / 95 / 129.99 / 130 / 139.99 / 140 | `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rechazados | 49, 251, -10, "abc" | Mensaje de error, no se guarda |

### HEART_RATE — Frecuencia cardiaca (bpm)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 72 | `NORMAL` |
| Precaucion baja | 55 | `WARNING` |
| Precaucion alta | 100 | `WARNING` |
| Malo bajo | 45 | `CRITICAL` |
| Malo alto | 110 | `CRITICAL` |
| Fronteras | 49.99 / 50 / 59.99 / 60 / 90 / 90.01 / 105 / 105.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rechazados | 29, 221, "abc" | Mensaje de error, no se guarda |

### GLUCOSE — Glucosa en ayunas (mg/dL)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 90 | `NORMAL` |
| Precaucion baja | 65 | `WARNING` |
| Precaucion alta | 110 | `WARNING` |
| Malo bajo | 55 | `CRITICAL` |
| Malo alto | 130 | `CRITICAL` |
| Fronteras | 59.99 / 60 / 69.99 / 70 / 99.99 / 100 / 125 / 125.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rechazados | 29, 401, "abc" | Mensaje de error, no se guarda |

### TEMP — Temperatura corporal (°C)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 36.6 | `NORMAL` |
| Precaucion baja | 35.8 | `WARNING` |
| Precaucion alta | 37.6 | `WARNING` |
| Malo bajo | 35.0 | `CRITICAL` |
| Malo alto | 38.4 | `CRITICAL` |
| Fronteras | 35.49 / 35.5 / 36.09 / 36.1 / 37.29 / 37.3 / 37.99 / 38.0 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rechazados | 31.9, 43.1, "abc" | Mensaje de error, no se guarda |

### SLEEP — Horas de sueño (hrs)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 7.5 | `NORMAL` |
| Precaucion baja | 6.0 | `WARNING` |
| Precaucion alta | 9.5 | `WARNING` |
| Malo bajo | 4.5 | `CRITICAL` |
| Malo alto | 11 | `CRITICAL` |
| Fronteras | 5.49 / 5.5 / 6.99 / 7.0 / 9.0 / 9.01 / 10.0 / 10.01 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` / `NORMAL` / `WARNING` / `WARNING` / `CRITICAL` |
| Rechazados | -1, 24.5, "abc" | Mensaje de error, no se guarda |

### MOOD — Estado conductual (pts, 1 a 5)

| Caso | Valor | Resultado esperado |
| ----- | ----- | ----- |
| Estable | 4.5 | `NORMAL` |
| Precaucion | 3.0 | `WARNING` |
| Malo | 2.0 | `CRITICAL` |
| Fronteras | 2.49 / 2.5 / 3.99 / 4.0 | `CRITICAL` / `WARNING` / `WARNING` / `NORMAL` |
| Rechazados | 0.5, 5.5, "abc" | Mensaje de error, no se guarda |

Cada captura valida agrega el valor al final de la grafica de 7 lecturas y saca la mas antigua.

## 2. Test PVT (afecta solo a MOOD)

Partiendo de `MOOD` = 4.5:

| Tiempo de reaccion | Resultado esperado |
| ----- | ----- |
| 300 ms o 380 ms | Sin cambio |
| 381 ms o 500 ms | `MOOD` = 3.0 (`WARNING`) |
| 501 ms o mas | `MOOD` = 2.0 (`CRITICAL`) |
| 450 ms con `MOOD` ya en 2.0 | Sin cambio y sin lectura nueva en la grafica |
| Toque antes del estimulo verde | Aviso "Salida en falso — reintentar", sin registro |

## 3. Escenarios del simulador

| Escenario | Resultado esperado |
| ----- | ----- |
| Nominal | Los 6 indicadores en `NORMAL` y graficas con sus 7 lecturas iniciales |
| Pico de radiacion | Solo `TEMP` = 38.4 (`CRITICAL`); zona metabolica/endocrina en alerta |
| Fatiga por aislamiento | Solo `SLEEP` = 4.5 y `MOOD` = 2.0 (`CRITICAL`); zona cabeza/mente en alerta |

## 4. Contramedida

Con un indicador en `WARNING` o `CRITICAL`, confirmar la contramedida debe regresar el valor a su valor nominal (`NORMAL`, verde) y agregar ese valor a la grafica. El flujo completo de la demo esta en el Blueprint, seccion 8, Fase 3.
