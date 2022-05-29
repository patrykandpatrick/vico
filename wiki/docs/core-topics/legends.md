A [`Legend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-legend/) describes the elements present on a chart.

## Adding a legend to a chart

Here’s how to add a [`Legend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-legend/) to a chart:

- In Jetpack Compose, use the `legend` parameter of the [`Chart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.chart/-chart) composable function.
- In the view system, use the `legend` field of [`BaseChartView`](https://patrykandpatryk.com/vico/api/vico/view/com.patrykandpatryk.vico.view.chart/-base-chart-view/) (which is extended by [`ChartView`](https://patrykandpatryk.com/vico/api/vico/view/com.patrykandpatryk.vico.view.chart/-chart-view/) and [`ComposedChartView`](https://patrykandpatryk.com/vico/api/vico/view/com.patrykandpatryk.vico.view.chart/-composed-chart-view/)).

## [`VerticalLegend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-vertical-legend/)

Vico includes one [`Legend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-legend/)
implementation: [`VerticalLegend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-vertical-legend/),
which displays its items in a vertical list. Each item of a [`VerticalLegend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-vertical-legend/)
comprises an icon and a label. [`VerticalLegend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-vertical-legend/) offers
multiple customization options (see the API reference). If using Jetpack Compose, you can create a [`VerticalLegend`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.legend/-vertical-legend/)
via the [`verticalLegend`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.legend/vertical-legend) composable function.

## Examples

See [the sample app](https://github.com/patrykandpatryk/vico/tree/master/sample) for examples in both Jetpack Compose and the view system.
In particular, see [`LineChartWithLabelsInside.kt`](https://github.com/patrykandpatryk/vico/blob/master/sample/src/main/java/com/patrykandpatryk/vico/sample/chart/LineChartWithLabelsInside.kt).
