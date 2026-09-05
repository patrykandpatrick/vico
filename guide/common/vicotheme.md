---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/vicotheme
---

# VicoTheme

[`VicoTheme`][vico-theme] houses default chart colors. Functions like [`rememberColumnCartesianLayer`][remember-column-cartesian-layer] and [`rememberLineCartesianLayer`][remember-line-cartesian-layer] use these for their default arguments. You can use [`ProvideVicoTheme`][provide-vico-theme] to provide a custom instance. Use [`vicoTheme`][vico-theme-2] to access the provided value (for example, `vicoTheme.textColor`).

```kt
ProvideVicoTheme(remember(/* ... */) { VicoTheme(/* ... */) }) { /* ... */ }
```

[vico-theme]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-vico-theme/
[remember-column-cartesian-layer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-column-cartesian-layer.html
[remember-line-cartesian-layer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-line-cartesian-layer.html
[provide-vico-theme]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-provide-vico-theme.html
[vico-theme-2]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/vico-theme.html
