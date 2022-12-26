A [`Legend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-legend/) describes the elements present on a chart.

## Adding a legend to a chart

Here’s how to add a [`Legend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-legend/) to a chart:

- In Jetpack Compose, use the `legend` parameter of the [`Chart`](https://patrykandpatrick.com/vico/api/vico/compose/com.patrykandpatrick.vico.compose.chart/-chart) composable function.
- In the view system, use the `legend` field of [`BaseChartView`](https://patrykandpatrick.com/vico/api/vico/views/com.patrykandpatrick.vico.views.chart/-base-chart-view/) (which is extended by [`ChartView`](https://patrykandpatrick.com/vico/api/vico/views/com.patrykandpatrick.vico.views.chart/-chart-view/) and [`ComposedChartView`](https://patrykandpatrick.com/vico/api/vico/views/com.patrykandpatrick.vico.views.chart/-composed-chart-view/)).

## [`VerticalLegend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-vertical-legend/)

Vico includes one [`Legend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-legend/)
implementation: [`VerticalLegend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-vertical-legend/),
which displays its items in a vertical list. Each item of a [`VerticalLegend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-vertical-legend/)
comprises an icon and a label. [`VerticalLegend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-vertical-legend/) offers
multiple customization options (see the API reference). If using Jetpack Compose, you can create a [`VerticalLegend`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.legend/-vertical-legend/)
via the [`verticalLegend`](https://patrykandpatrick.com/vico/api/vico/compose/com.patrykandpatrick.vico.compose.legend/vertical-legend) composable function.

## Examples

See [the sample app](https://github.com/patrykandpatrick/vico/tree/master/sample) for examples in both Jetpack Compose and the view system.
In particular, see [`LineChartWithLabelsInside.kt`](https://github.com/patrykandpatrick/vico/blob/master/sample/src/main/java/com/patrykandpatrick/vico/sample/chart/LineChartWithLabelsInside.kt).
