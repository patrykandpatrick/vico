# Vico - Agent Instructions

## Overview

Vico is a multiplatform chart library written in Kotlin, supporting Compose
Multiplatform.

## Build & Test

JDK 17 required. Build targets JVM 11.

```bash
# Compile
./gradlew compileDebugSources

# Test
./gradlew test
```

For code changes, run the relevant compile and test tasks before committing when
practical. Documentation-only and other non-code changes generally do not need a
full test run. Formatting is handled by the Lefthook pre-commit hook and
enforced by CI on every push and PR.

## Key Guidance

1. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
