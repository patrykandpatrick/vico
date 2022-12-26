Chart data in Vico is stored in [`ChartEntryModel`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry-model/)s. A [`ChartEntry`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry) holds data about an individual entry on a chart (for example, a single column).

## Static data

For static data (that is, data that remains unchanged), create a `ChartEntryModel` instance (for simple charts) or a [`ComposedChartEntryModel`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.chart.composed/-composed-chart-entry-model/) instance (for composed charts). `ComposedChartEntryModel` extends `ChartEntryModel`. Factory functions are available, including [`entryModelOf`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/entry-model-of?query=fun%20entryModelOf(vararg%20entries:%20Pair%3CNumber,%20Number%3E):%20ChartEntryModel) and [`composedChartEntryModelOf`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry.composed/-composed-chart-entry-model-producer/-companion/composed-chart-entry-model-of). You can apply a `ChartEntryModel` or `ComposedChartEntryModel` to a chart as follows:

- In Jetpack Compose, use the `model` parameter of the `Chart` composable.

- In the view system, use the `model` field of `BaseChartView` (which is extended by `ChartView` and `ComposedChartView`).

## Dynamic data

For dynamic data (that is, data that may change), use [`ChartModelProducer`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-model-producer/?query=interface%20ChartModelProducer%3CModel%20:%20ChartEntryModel%3E)s. Every `ChartModelProducer` implementation has a `getModel` function that returns a [`ChartEntryModel`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry-model/), and a `progressModel` function for difference animations. The `progressModel` function calculates intermediate lists of entries, which should happen on a thread other than the main one, so as not to cause lags. In addition, `ChartModelProducer` declares two listener-related functions: `registerForUpdates` and `unregisterFromUpdates`.

Vico includes two default `ChartModelProducer` implementations: [`ChartEntryModelProducer`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry-model-producer/?query=class%20ChartEntryModelProducer(entryCollections:%20List%3CList%3CChartEntry%3E%3E,%20backgroundExecutor:%20Executor)%20:%20ChartModelProducer%3CChartEntryModel%3E) (for simple charts) and [`ComposedChartEntryModelProducer`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry.composed/-composed-chart-entry-model-producer/?query=class%20ComposedChartEntryModelProducer%3CModel%20:%20ChartEntryModel%3E(chartModelProducers:%20List%3CChartModelProducer%3CModel%3E%3E,%20backgroundExecutor:%20Executor)%20:%20ChartModelProducer%3CComposedChartEntryModel%3CModel%3E%3E) (for composed charts). You can create an instance of either and apply it to a chart as follows:

- In Jetpack Compose, use the `chartModelProducer` parameter of the `Chart` composable.

- In the view system, use the `entryProducer` field of `BaseChartView` (which is extended by `ChartView` and `ComposedChartView`).

Differences are animated by default (see [`DefaultDiffProcessor`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry.diff/-default-diff-processor/?query=class%20DefaultDiffProcessor%20:%20DiffProcessor%3CChartEntry%3E)).

## Examples

See [the README](https://github.com/patrykandpatrick/vico#readme) and [`ShowcaseViewModel` in the sample app](https://github.com/patrykandpatrick/vico/blob/master/sample/src/main/java/com/patrykandpatrick/vico/sample/viewsmodel/ShowcaseViewModel.kt) for multiple examples.
