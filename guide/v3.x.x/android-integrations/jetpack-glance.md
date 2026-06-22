# Jetpack Glance

The `compose-glance` module lets Jetpack Glance app widgets display Vico charts. Use [`CartesianChartImage`](https://api.vico.patrykandpatrick.com/vico/compose-glance/com.patrykandpatrick.vico.compose.glance.cartesian/-cartesian-chart-image.html) and [`PieChartImage`](https://api.vico.patrykandpatrick.com/vico/compose-glance/com.patrykandpatrick.vico.compose.glance.pie/-pie-chart-image.html) in widget content. These functions render charts off-screen to bitmaps and display the result as a static Glance image.

This differs from the interactive chart hosts [`CartesianChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart-host) and [`PieChartHost`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart-host/). Glance doesn’t support the Compose canvas, so the image functions are the supported entry points for app widgets. Because the result is static, chart interactions such as scroll, zoom, and markers are unavailable.

## Cartesian charts

Pass a [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/) instance and a [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/) instance:

```kt
CartesianChartImage(
    chart = rememberCartesianChart(rememberLineCartesianLayer()),
    model = model,
    contentDescription = "Weekly sales",
    modifier = GlanceModifier.fillMaxSize(),
)
```

## Pie charts

Pass a [`PieChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie/-pie-chart/) instance and a [`PieChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.pie.data/-pie-chart-model/) instance:

```kt
PieChartImage(
    chart = rememberPieChart(),
    model = model,
    contentDescription = "Expense breakdown",
    modifier = GlanceModifier.fillMaxSize(),
)
```

## Size and scaling

By default, each image uses Glance’s current widget size. You can pass a custom [`DpSize`](https://developer.android.com/reference/kotlin/androidx/compose/ui/unit/DpSize) value via the `size` parameter. Use the `contentScale` parameter, which accepts a [`ContentScale`](https://developer.android.com/reference/kotlin/androidx/glance/layout/ContentScale) value, to control how the bitmap is scaled within the image bounds.
