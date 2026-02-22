---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/getting-started
---

# Getting started

## Prerequisites

Ensure the following:

* The Maven Central repository is added to your project.
* For Android, `minSdk` is set to at least 23.

## Dependencies

Add only the modules you need.

```toml
[versions]
vico = "3.0.0"

[libraries]
# Compose Multiplatform
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }

# Material 2 theming in Compose Multiplatform
vico-compose-m2 = { group = "com.patrykandpatrick.vico", name = "compose-m2", version.ref = "vico" }

# Material 3 theming in Compose Multiplatform
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }

# Android view system
vico-views = { group = "com.patrykandpatrick.vico", name = "views", version.ref = "vico" }
```

```kt
dependencies {
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
    implementation(libs.vico.views)
}
```
