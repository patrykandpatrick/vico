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

**Run both before committing.** Formatting is handled by the Lefthook
pre-commit hook and enforced by CI on every push and PR.

## Key Guidance

1. **Dual implementation stacks:** `vico/compose` (Compose Multiplatform) and
   `vico/views` (Android Views) are parallel implementations. Determine whether
   changes need to be applied to both.
2. **Explicit API mode:** All library modules require explicit visibility
   modifiers on public API.

## Documentation Writing

When editing guide prose:

1. **Link first mentions:** On each page, the first mention of a code element
   should link to the API reference.
2. **Avoid nounifying code names:** Don’t use code element names as generic
   count nouns (for example, avoid phrasing like “a `Foo`,” “the `Bar`,” or
   plural forms like `Baz`s). If you need to refer to an instance, attach the
   article to a generic noun instead (for example, “a `Foo` instance”).
3. **Distinguish classes from instances:** If the prose refers to an object, say
   “instance” or use a generic noun like “chart,” “model,” “layer,” “marker,” or
   “transaction,” and introduce the code element name separately if needed.
4. **Prefer generic nouns for concepts:** Use ordinary language for the concept
   being discussed, and reserve code names for precise references to API
   elements.
5. **Keep terminology parallel:** In comparisons and lists, phrase equivalent
   ideas symmetrically rather than mixing generic terms on one side and code
   names on the other.
6. **Apply changes across stacks:** If equivalent Compose and Views guide pages
   exist, keep the wording aligned unless platform differences require
   otherwise.
