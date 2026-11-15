# SoftieCare
Repository for the team project at the NASA Space Apps Challenge.

Team: "Softies"

Members:
- Emir Alexander Alcantar Martinez
- Carlos Alberto Jaime Mascareño
- Elias Tamayo Salcedo
- Arturo Rafael Cornejo Escobar

Offline-First Android app (Kotlin + Jetpack Compose) for astronaut medical self-assessment.

The single source of truth is [`docs/blueprint_softiecare.md`](docs/blueprint_softiecare.md). Read it in full before writing code.

Values to test each level (stable, caution, bad): [`docs/data_guide.md`](docs/data_guide.md).

All interface text, code comments and documentation are in English.

## Requirements

- Recent Android Studio with `compileSdk 37` installed (SDK Manager)
- Git

## First use

```
git clone https://github.com/TitaniumCJ/SoftieCare.git
cd SoftieCare
git checkout feature/Modulo<N>
```

Open the folder in Android Studio (*File > Open*), wait for the Gradle sync and build with *Build > Assemble Project*.

## Branches

| Branch | Module | Owner | Allowed folder |
| ----- | ----- | ----- | ----- |
| `Main` | Integration | Carlos | Everything, only through Pull Requests |
| `feature/Modulo1` | 1. Core, Clinical Engine and Simulator | Carlos | `core/`, `ui/`, `features/simulator/`, `MainActivity.kt` |
| `feature/Modulo2` | 2. Main HUD and Navigation | Emir | `features/dashboard/`, `res/drawable/` |
| `feature/Modulo3` | 3. Guided Measurement and PVT | Elias | `features/assessment/` |
| `feature/Modulo4` | 4. Diagnosis, Countermeasures and Charts | Arturo | `features/countermeasures/` |

Rules (Blueprint, sections 4, 8 and 9):

- Work only inside your module folder.
- Do not commit `MainActivity.kt`. If you edit it to test on the emulator, revert it before committing:
  `git restore app/src/main/java/com/softiecare/app/MainActivity.kt`
- Test screens with `@Preview`.
- Follow the signatures in section 4.2 and use only the colors in `ui/theme/Color.kt`.

## Daily workflow

```
git pull origin feature/Modulo<N>
# work
git add app/src/main/java/com/softiecare/app/features/<module>/
git commit -m "Short description of the change"
git push origin feature/Modulo<N>
```

When the module is done, open a Pull Request to `Main` after checking the checklist in section 9 of the Blueprint.
