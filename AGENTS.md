# Vico - Agent Instructions

## Overview

Vico is a multiplatform chart library written in Kotlin, supporting Compose
Multiplatform (iOS, Desktop, Web/WASM) and Android Views.

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

1. **Dual implementation stacks:** `vico/compose` (Compose Multiplatform) and
   `vico/views` (Android Views) are parallel implementations. Determine whether
   changes need to be applied to both.
2. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
