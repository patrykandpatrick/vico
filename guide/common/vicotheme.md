---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/vicotheme
---

# VicoTheme

[`VicoTheme`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-vico-theme/) houses default chart colors. Functions like [`rememberColumnCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-column-cartesian-layer) and [`rememberLineCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-line-cartesian-layer) use these for their default arguments. You can use [`ProvideVicoTheme`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-provide-vico-theme) to provide a custom instance. Use [`vicoTheme`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/vico-theme) to access the provided value (for example, `vicoTheme.textColor`).

```kt
ProvideVicoTheme(remember(/* ... */) { VicoTheme(/* ... */) }) { /* ... */ }
```
