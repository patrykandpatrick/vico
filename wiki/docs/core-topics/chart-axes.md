You can display axes along each of the four edges of a chart.

## Creating axes

Axes can be added to a chart as follows:

- In Jetpack Compose, use the `*axis` parameters of the [`Chart`](https://patrykandpatrick.com/vico/api/vico/compose/com.patrykandpatrick.vico.compose.chart/-chart) composable, and create axes via the provided `*axis` composable functions (e.g., [`bottomAxis`](https://patrykandpatrick.com/vico/api/vico/compose/com.patrykandpatrick.vico.compose.axis.horizontal/bottom-axis)).

- In the view system, axes can be added to charts via [XML attributes](../../customization/customization-in-the-view-system) or programmatically (in which case we recommend using the `create*Axis` builder functions).

## Customizing axes

You can customize the appearance of charts axes, including the style of the line, the ticks, the labels, and more. You can also define titles for axes. Axes can be customized as follows:

- In Jetpack Compose, each of the four `*axis` composable functions has parameters that allow for customization.

- In the view system, axes can be customized via [XML attributes](../../customization/customization-in-the-view-system) or programmatically. For an example of programmatic customization, see [`ViewStackedColumnChart`](https://github.com/patrykandpatrick/vico/blob/master/sample/src/main/java/com/patrykandpatrick/vico/sample/chart/StackedColumnChart.kt#L57) in the sample app.

Most notably, [`HorizontalAxis`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.horizontal/-horizontal-axis/), when `tickPosition` is set to [`HorizontalAxis.TickPosition.Center`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.horizontal/-horizontal-axis/-tick-position/-center/), allows for the tick offset and spacing to be specified. This enables you to add intermediate entries between labels. For example, a chart could include 28 days’ worth of data, but only four labels, one for each week. See [`ColumnChart.kt`](https://github.com/patrykandpatrick/vico/blob/master/sample/src/main/java/com/patrykandpatrick/vico/sample/chart/ColumnChart.kt) in the sample app for an example of this.

To create entirely custom axes, you can write your own [`VerticalAxis`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.vertical/-vertical-axis/) and [`HorizontalAxis`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.horizontal/-horizontal-axis/) implementations.

## [`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter/)s

[`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter/)s ([`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter/) inherits from [`ValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.formatter/-value-formatter/), described in “[Value formatters](../value-formatters)”) enable you to format the values displayed along chart axes. Three [`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter/) implementations are included in the library: [`DecimalAxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-decimal-format-axis-value-formatter/), [`DefaultAxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-default-axis-value-formatter/), and [`PercentageFormatAxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-percentage-format-axis-value-formatter/). You can create your own implementations. To define an [`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter/) for a chart axis, do the following:

- In Jetpack Compose, use the `valueFormatter` parameter of any of the four `*axis` composables.

- In the view system, use the `valueFormatter` field of [`Axis`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis/-axis/) (see [here](https://github.com/patrykandpatrick/vico/blob/master/sample/src/main/java/com/patrykandpatrick/vico/sample/chart/ColumnChart.kt#L96)).

## Examples

See [the sample app](https://github.com/patrykandpatrick/vico/tree/master/sample) for multiple examples in both Jetpack Compose and the view system.
