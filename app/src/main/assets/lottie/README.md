# Animaciones de procedimiento

`ProcedureAnimation` (ui/components) carga de esta carpeta un archivo por indicador. El nombre es el id en minusculas:

| Archivo | Indicador | Que debe mostrar |
| ----- | ----- | ----- |
| `bp_sys.json` | Presion sistolica | Brazalete en el brazo a la altura del corazon |
| `heart_rate.json` | Frecuencia cardiaca | Sensor de pulso en el dedo |
| `glucose.json` | Glucosa | Gota del dedo a la tira del glucometro |
| `temp.json` | Temperatura | Termometro bajo la lengua |
| `sleep.json` | Sueño | Horas de sueño (reloj o luna) |
| `mood.json` | Estado conductual | Escala de animo y alerta |

Reglas:

- Formato: Lottie JSON (no dotLottie).
- Solo animaciones gratuitas de LottieFiles (Lottie Simple License) o propias.
- Revisar que la animacion muestre el procedimiento correcto antes de agregarla.
- Si falta un archivo, la app no falla: simplemente no muestra animacion.

## Creditos

| Archivo | Animacion | Autor | URL |
| ----- | ----- | ----- | ----- |
