# Getting started

## Prerequisites

Ensure the following:

- The Maven Central repository is added to your project.
- For Android, `minSdk` is set to at least 23.

## Dependencies

```toml
[versions]
vico = "3.2.3"

[libraries]
vico-views = { group = "com.patrykandpatrick.vico", name = "views", version.ref = "vico" }
```

```kt
dependencies {
    implementation(libs.vico.views)
}
```
