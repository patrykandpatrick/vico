# Jetpack Glance

The `compose-glance` module lets Jetpack Glance app widgets display Vico charts. Use [`CartesianChartImage`][cartesian-chart-image] and [`PieChartImage`][pie-chart-image] in widget content. These functions render charts off-screen to bitmaps and display the result as a static Glance image.

This differs from the interactive chart hosts [`CartesianChartHost`][cartesian-chart-host] and [`PieChartHost`][pie-chart-host]. Glance doesn’t support the Compose canvas, so the image functions are the supported entry points for app widgets. Because the result is static, chart interactions such as scroll, zoom, and markers are unavailable.

## Cartesian charts

Pass a [`CartesianChart`][cartesian-chart] instance and a [`CartesianChartModel`][cartesian-chart-model] instance:

```kt
CartesianChartImage(
    chart = rememberCartesianChart(rememberLineCartesianLayer()),
    model = model,
    contentDescription = "Weekly sales",
    modifier = GlanceModifier.fillMaxSize(),
)
```

## Pie charts

Pass a [`PieChart`][pie-chart] instance and a [`PieChartModel`][pie-chart-model] instance:

```kt
PieChartImage(
    chart = rememberPieChart(),
    model = model,
    contentDescription = "Expense breakdown",
    modifier = GlanceModifier.fillMaxSize(),
)
```

## Size and scaling

By default, each image uses Glance’s current widget size. You can pass a custom [`DpSize`][dp-size] value via the `size` parameter. Use the `contentScale` parameter, which accepts a [`ContentScale`][content-scale] value, to control how the bitmap is scaled within the image bounds.

[cartesian-chart-image]: https://api.vico.patrykandpatrick.com/vico/compose-glance/com.patrykandpatrick.vico.compose.glance.cartesian/-cartesian-chart-image.html
[pie-chart-image]: https://api.vico.patrykandpatrick.com/vico/compose-glance/com.patrykandpatrick.vico.compose.glance.pie/-pie-chart-image.html
[cartesian-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host.html
[pie-chart-host]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host.html
[cartesian-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/
[cartesian-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/
[pie-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/
[pie-chart-model]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/
[dp-size]: https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/DpSize
[content-scale]: https://developer.android.com/reference/kotlin/androidx/glance/layout/ContentScale
