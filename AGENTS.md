# Vico - Agent Instructions

## Overview

Vico is a multiplatform chart library written in Kotlin, supporting Compose
Multiplatform (Android, iOS, Desktop, Web/WASM).

## Build & Test

JDK 17 required. Build targets JVM 11.

```bash
# Compile
./gradlew compileDebugSources

# Test
./gradlew test
```

**Run both before committing.** Formatting is handled by the Lefthook pre-commit
hook and enforced by CI on every push and PR.

## Key Guidance

1. **Compose-first:** Active development happens in the Compose modules. Android
   Views support is maintained from the last shared-release branch only.
2. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
