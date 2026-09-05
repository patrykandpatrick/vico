---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/legend
---

# Legend

A legend, represented by [`Legend`][legend], describes the elements present on a chart. There are two built-in implementations: [`HorizontalLegend`][horizontal-legend] and [`VerticalLegend`][vertical-legend]. These use [`LegendItem`][legend-item] instances. Instantiate `HorizontalLegend` and `VerticalLegend` via [`rememberHorizontalLegend`][remember-horizontal-legend] and [`rememberVerticalLegend`][remember-vertical-legend]:

```kt
rememberCartesianChart(legend = rememberHorizontalLegend(/* ... */), /* ... */)
```

```kt
rememberCartesianChart(legend = rememberVerticalLegend(/* ... */), /* ... */)
```

[legend]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-legend/
[horizontal-legend]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-horizontal-legend/
[vertical-legend]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-vertical-legend/
[legend-item]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-legend-item/
[remember-horizontal-legend]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/remember-horizontal-legend.html
[remember-vertical-legend]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/remember-vertical-legend.html
