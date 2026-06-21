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

Add only the modules you need. `compose-m2` and `compose-m3` provide Material 2 and Material 3 theming, respectively.

```toml
[versions]
vico = "3.2.3"

[libraries]
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m2 = { group = "com.patrykandpatrick.vico", name = "compose-m2", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
```

```kt
dependencies {
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m2)
    implementation(libs.vico.compose.m3)
}
```
