# Vico - Agent Instructions

## Project Overview

Vico is a powerful and extensible multiplatform chart library written in Kotlin. It supports:

- Jetpack Compose (Android)
- Compose Multiplatform (iOS, Desktop, Web, WASM)
- Android View System

**Languages:** Kotlin  
**Build System:** Gradle 9.2+ with Kotlin DSL  
**Java Version:** JDK 17 (Zulu distribution recommended)

## Repository Structure

```
vico/                           # Root project
├── vico/                       # Main library modules
│   ├── core/                   # Core library (Android-only, shared logic for Views/Compose)
│   ├── compose/                # Jetpack Compose support (depends on core)
│   ├── compose-m2/             # Material 2 theming for Compose
│   ├── compose-m3/             # Material 3 theming for Compose
│   ├── views/                  # Android Views support
│   ├── multiplatform/          # Compose Multiplatform support (independent implementation)
│   ├── multiplatform-m2/       # Material 2 for multiplatform
│   └── multiplatform-m3/       # Material 3 for multiplatform
├── sample/                     # Sample applications
│   ├── app/                    # Multiplatform sample app (Android, iOS, Desktop, Web)
│   ├── compose/                # Android Compose samples
│   ├── multiplatform/          # Shared multiplatform sample code
│   └── views/                  # Android Views samples
├── buildSrc/                   # Gradle convention plugins and versions
│   └── src/main/kotlin/
│       ├── Versions.kt         # Version constants (COMPILE_SDK, MIN_SDK, VICO)
│       ├── LibraryExtension.kt # Android library configuration
│       ├── Project.kt          # Project extensions (moduleNamespace)
│       ├── dokka-convention.gradle.kts
│       └── publishing-convention.gradle.kts
└── gradle/libs.versions.toml   # Version catalog for dependencies
```

## Build Commands

### Full Build (CI command)

```bash
./gradlew build -x lint -x test
```

**Time:** ~10-15 minutes for first build; ~2-5 minutes with Gradle cache

### Run Tests

```bash
./gradlew test
```

**Time:** ~30-60 seconds  
Tests are located in `vico/core/src/test/java/`.

### Format Code (ktfmt)

The project uses [ktfmt](https://github.com/facebook/ktfmt) with Google style:

```bash
curl -sSL -o ktfmt.jar https://github.com/facebook/ktfmt/releases/download/v0.59/ktfmt-0.59-with-dependencies.jar
java -jar ktfmt.jar --google-style .
```

### Build Android Sample APK

```bash
./gradlew :sample:app:assembleDebug
```

Output: `sample/app/build/outputs/apk/debug/app-debug.apk`

## CI Workflows (GitHub Actions)

All workflows run on `push` and `pull_request`:

| Workflow         | File                                     | What it does                                                |
| ---------------- | ---------------------------------------- | ----------------------------------------------------------- |
| Build            | `.github/workflows/build.yml`            | `./gradlew build -x lint -x test`                           |
| Test             | `.github/workflows/test.yml`             | `./gradlew test`                                            |
| Check formatting | `.github/workflows/check-formatting.yml` | ktfmt with `--dry-run --google-style --set-exit-if-changed` |

**Always run these three commands before committing:**

1. `./gradlew build -x lint -x test`
2. `./gradlew test`
3. `java -jar ktfmt.jar --google-style .`

## Code Style

- **Formatter:** ktfmt with Google style
- **Indent:** 2 spaces for Kotlin/KTS files
- **Max line length:** 100 characters
- **Trailing commas:** Allowed
- Configuration: `.editorconfig`

## Key Source Locations

| Component                  | Path                                                                |
| -------------------------- | ------------------------------------------------------------------- |
| Android Core classes       | `vico/core/src/main/java/com/patrykandpatrick/vico/core/cartesian/` |
| Android Common utilities   | `vico/core/src/main/java/com/patrykandpatrick/vico/core/common/`    |
| Android Compose components | `vico/compose/src/main/java/`                                       |
| Multiplatform Core classes | `vico/multiplatform/src/commonMain/kotlin/`                         |
| Android Views components   | `vico/views/src/main/java/`                                         |
| Unit tests (Android Core)  | `vico/core/src/test/java/`                                          |
| Dependency versions        | `gradle/libs.versions.toml`                                         |
| Build versions             | `buildSrc/src/main/kotlin/Versions.kt`                              |

## Important Notes

1. **Architecture:** The project currently maintains two parallel implementations:
    - `vico/core`, `vico/compose`, `vico/views`: Android-only implementation (`vico/core` uses
      `android.graphics` APIs).
    - `vico/multiplatform`: Multiplatform implementation (Pure Compose core). When making changes,
      check if they need to be applied to both implementations.
2. **Explicit API mode:** All library modules use `explicitApi()` - public API must have explicit
   visibility modifiers.
3. **Dependency Management:** Use `gradle/libs.versions.toml` for managing dependencies and
   versions.
4. **JVM Target:** JVM 11 for all modules.
5. **Android SDK:** compileSdk=36, minSdk=23 (defined in `Versions.kt`).
6. **Multiplatform targets:** Android, iOS (arm64, x64, simulatorArm64), Desktop (JVM), JS, WASM.
7. **Test framework:** JUnit 5 (Jupiter) with MockK for mocking.

## Troubleshooting

- **Slow first build:** Initial Gradle/dependency downloads can take 5-10 minutes.
- **Test deprecation warnings:** Some test methods call deprecated APIs - these warnings are
  expected.
