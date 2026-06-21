# Vico - Agent Instructions

## Overview

This branch maintains Vico’s Android Views artifact. Active Compose development
happens on the main branch.

## Build & Test

JDK 17 required. Build targets JVM 11.

```bash
# Compile
./gradlew :vico:views:compileDebugSources

# Build the Android sample
./gradlew :sample:android:assembleDebug

# Test
./gradlew test
```

**Run both before committing.** Formatting is handled by the Lefthook pre-commit
hook and enforced by CI on every push and PR.

## Key Guidance

1. **Views maintenance:** Only critical Android Views fixes belong on this
   branch. Feature work and Compose library changes belong on the main branch.
2. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.
3. **Sample scope:** The sample app may use Compose as its host UI, but sample
   chart implementations should exercise `vico:views`.
