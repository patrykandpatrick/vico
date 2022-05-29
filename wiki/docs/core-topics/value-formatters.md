[`ValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/)s
format numeric values for display. A [`ValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/)
implementation must contain a `formatValue` function with two parameters: `value` (the value to format) and `chartValues`
(the [`ChartValues`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.chart.values/-chart-values/) instance
used by the chart from which the value comes). The return type of this function is `CharSequence`.

Three [`ValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/) implementations
are included in the library: [`DecimalFormatValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-decimal-format-value-formatter/),
[`DefaultValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-default-value-formatter/), and
[`PercentageFormatValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-percentage-format-value-formatter/).
You can create your own implementations.

[`ValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/)s
are used for data labels in line charts and column charts. For chart axes, use [`AxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/)s
(see “[Chart axes](../chart-axes).”) The [`AxisValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/) interface inherits from
[`ValueFormatter`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.formatter/-value-formatter/).
