---
metaLinks:
  alternates:
    - https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/common/legend
---

# Legend

A [`Legend`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-legend/) describes the elements present on a chart. There are two built-in implementations: [`HorizontalLegend`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-horizontal-legend/) and [`VerticalLegend`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-vertical-legend/). These `Legend`s consist of [`LegendItem`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/-legend-item/)s. Instantiate `HorizontalLegend` and `VerticalLegend` via [`rememberHorizontalLegend`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/remember-horizontal-legend) and [`rememberVerticalLegend`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.common/remember-vertical-legend):

```kt
rememberCartesianChart(legend = rememberHorizontalLegend(/* ... */), /* ... */)
```

```kt
rememberCartesianChart(legend = rememberVerticalLegend(/* ... */), /* ... */)
```
