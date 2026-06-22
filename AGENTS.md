# Vico—agent instructions

## Overview

Vico is a multiplatform chart library written in Kotlin, supporting Compose
Multiplatform.

## Build and test

JDK 17 is required. Builds target JVM 11.

```bash
# Compile
./gradlew compileDebugSources

# Test
./gradlew test
```

For code changes, run the relevant compile and test tasks before committing when
practical. Documentation-only and other non-code changes usually do not need a
full test run. Formatting is handled by the `Lefthook` pre-commit hook and
enforced by CI on every push and PR.

## Key guidance

1. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
