You can display axes along each of the four edges of a chart.

## Creating axes

Axes can be added to a chart as follows:

- In Jetpack Compose, use the `*axis` parameters of the [`Chart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.chart/-chart) composable, and create axes via the provided `*axis` composable functions (e.g., [`bottomAxis`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.axis.horizontal/bottom-axis)).

- In the view system, axes can be added to charts via [XML attributes](../../customization/customization-in-the-view-system) or programmatically (in which case we recommend using the `create*Axis` builder functions).

## Customizing axes

You can customize the appearance of charts axes, including the style of the line, the ticks, and the labels, as follows:

- In Jetpack Compose, each of the four `*axis` composable functions has parameters that allow for customization.

- In the view system, axes can be customized via [XML attributes](../../customization/customization-in-the-view-system) or programmatically.

To create entirely custom axes, you can write your own [`VerticalAxis`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.vertical/-vertical-axis/) and [`HorizontalAxis`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.horizontal/-horizontal-axis/) implementations.

## [`AxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-axis-value-formatter/)s

[`AxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-axis-value-formatter/)s enable you to format the values displayed along chart axes. For example, you could display temperatures and dates along the axes of a weather chart. Three [`AxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-axis-value-formatter/) implementations are included in the library: [`DecimalAxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-decimal-format-axis-value-formatter/), [`DefaultAxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-default-axis-value-formatter/), and [`PercentageFormatAxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.axis.formatter/-percentage-format-axis-value-formatter/). You can create your own implementations.

## Examples

See [the sample app](https://github.com/patrykandpatryk/vico/tree/master/sample) for multiple examples in both Jetpack Compose and the view system.
