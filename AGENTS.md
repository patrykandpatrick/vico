# Vico - Agent Instructions

## Overview

Vico is a multiplatform chart library written in Kotlin, supporting Compose
Multiplatform (iOS, Desktop, Web/WASM) and Android Views.

## Build & Test

JDK 17 required. Build targets JVM 11.

```bash
# Build
./gradlew build -x lint -x test

# Test
./gradlew test

# Format (ktfmt, Google style)
# See .github/workflows/check-formatting.yml for the current version and download URL.
java -jar ktfmt.jar --google-style .
```

**Run all three before committing.** CI enforces build, test, and formatting on
every push and PR.

## Key Guidance

1. **Dual implementation stacks:** `vico/compose` (Compose Multiplatform) and
   `vico/views` (Android Views) are parallel implementations. Determine whether
   changes need to be applied to both.
2. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
