# Customization in Jetpack Compose

Vico allows for each chart to be customized individually. For example, the `lineChart` function has
parameters for the line thickness, the line color, and so on. However, we recommend you create a common
chart style and override it if needed. You can do this as described below.

## `ProvideChartStyle`

`ProvideChartStyle` is similar to `MaterialTheme` from `compose.material` and `compose.material3`. It can be used as follows. To access
the provided value, use `currentChartStyle`.

```kt
ProvideChartStyle(chartStyle = ChartStyle(...)) {
   ...
}
```

Functions such as `lineChart` use the values provided via `ProvideChartStyle` as the defaults for their parameters.
For example, the default value for the `lines` parameter of `lineChart` is `currentChartStyle.lineChart.lines`.

## `m2ChartStyle` & `m3ChartStyle`

If you use `compose.material` or `compose.material3` in your app, you can use the `m2ChartStyle`
(`compose-m2` artifact) or `m3ChartStyle` (`compose-m3` artifact) function to create a baseline `ChartStyle`
implementation that uses the colors from your app’s Material Theme:

```kt
ProvideChartStyle(chartStyle = m3ChartStyle()) {
    ...
}
```

## `ChartStyle`

A `ChartStyle` instance comprises these fields:

| Field | Description |
| --- | --- |
| `axis` | The appearance of chart axes (a [`ChartStyle.Axis`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.style/-chart-style/-axis/) instance). |
| `columnChart` | The appearance of column charts (a [`ChartStyle.ColumnChart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.style/-chart-style/-column-chart/) instance). |
| `lineChart` | The appearance of line charts (a [`ChartStyle.LineChart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.style/-chart-style/-line-chart/) instance). |
| `marker` | The appearance of chart markers (a [`ChartStyle.Marker`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.style/-chart-style/-marker/) instance). |