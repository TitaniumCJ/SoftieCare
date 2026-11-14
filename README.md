# SoftieCare
Repositorio para la realizacion del proyecto 
en el evento de NASA SPACE APPS. 

Equipo:  "Softies" 

Integrantes: 
- Emir Alexander Alcantar Martinez
- Carlos Alberto Jaime Mascareño
- Elias Tamayo Salcedo
- Arturo Rafael Cornejo Escobar

App Android (Kotlin + Jetpack Compose) de autoevaluacion medica Offline-First para astronautas.

La fuente unica de verdad es [`docs/blueprint_softiecare.md`](docs/blueprint_softiecare.md). Leerlo completo antes de escribir codigo.

Valores para probar cada nivel (estable, precaucion, malo): [`docs/guia_datos.md`](docs/guia_datos.md).

## Requisitos

- Android Studio reciente con `compileSdk 37` instalado (SDK Manager)
- Git

## Primer uso

```
git clone https://github.com/TitaniumCJ/SoftieCare.git
cd SoftieCare
git checkout feature/Modulo<N>
```

Abrir la carpeta en Android Studio (*File > Open*), esperar el Gradle Sync y compilar con *Build > Assemble Project*.

## Ramas

| Rama | Modulo | Responsable | Carpeta permitida |
| ----- | ----- | ----- | ----- |
| `Main` | Integracion | Carlos | Todo, solo via Pull Request |
| `feature/Modulo1` | 1. Core, Motor Clinico y Simulador | Carlos | `core/`, `ui/`, `features/simulator/`, `MainActivity.kt` |
| `feature/Modulo2` | 2. Anatomia Interactiva y HUD | Emir | `features/dashboard/`, `res/drawable/` |
| `feature/Modulo3` | 3. Captura Guiada y Test PVT | Elias | `features/assessment/` |
| `feature/Modulo4` | 4. Diagnostico, Contramedidas y Graficas | Arturo | `features/countermeasures/` |

Reglas (Blueprint, secciones 4, 8 y 9):

- Trabajar solo dentro de la carpeta del modulo.
- No hacer commit de `MainActivity.kt`. Si se edita para probar en el emulador, revertirlo antes del commit:
  `git restore app/src/main/java/com/softiecare/app/MainActivity.kt`
- Probar las pantallas con `@Preview`.
- Respetar las firmas de la seccion 4.2 y usar solo los colores de `ui/theme/Color.kt`.

## Flujo diario

```
git pull origin feature/Modulo<N>
# trabajar
git add app/src/main/java/com/softiecare/app/features/<modulo>/
git commit -m "Descripcion corta del cambio"
git push origin feature/Modulo<N>
```

Al terminar el modulo, abrir un Pull Request hacia `Main` y revisar antes el checklist de la seccion 9 del Blueprint.
