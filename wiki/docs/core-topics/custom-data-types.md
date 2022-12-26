You may need to display different types of data on a chart: dates, temperatures, etc. You can achieve this by creating custom [`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter) and [`ChartEntry`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry) implementations.

## Example overview

In this example, we’ll display display values for four arbitrary dates.

First, write a custom [`ChartEntry`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry) implementation with a `localDate` field. Then, build a map where consecutive integers are linked to instances of your custom [`ChartEntry`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry) implementation, and create a [`ChartEntryModelProducer`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.entry/-chart-entry-model-producer/) to provide this data.

```kotlin
class Entry(
    val localDate: LocalDate,
    override val x: Float,
    override val y: Float,
) : ChartEntry {

    override fun withY(y: Float) = Entry(
        localDate = this.localDate,
        x = this.x,
        y = y,
    )
}

val chartEntryModelProducer = listOf(
    "2022-07-14" to 1.3f,
    "2022-07-15" to 2.5f,
    "2022-07-17" to 2.5f,
    "2022-08-01" to 3f,
).mapIndexed { index, (dateString, y) ->

    Entry(
        localDate = LocalDate.parse(dateString),
        x = index.toFloat(),
        y = y,
    )
}.let { entryCollection -> ChartEntryModelProducer(entryCollection) }
```

To display days of the month (rather than one of the consecutive integers used as the keys of the `entries` map) along the bottom axis, create an [`AxisValueFormatter`](https://patrykandpatrick.com/vico/api/vico/core/com.patrykandpatrick.vico.core.axis.formatter/-axis-value-formatter). See [“`AxisValueFormatter`s” under “Chart axes”](/core-topics/chart-axes/#axisvalueformatters) for more information.

```kotlin
AxisValueFormatter<AxisPosition.Horizontal> { value, chartValues ->
    (chartValues.chartEntryModel.entries[0].getOrNull(value.toInt()) as Entry?)
        ?.localDate
        ?.run { "$dayOfMonth/$monthValue" }
        .orEmpty()
}
```

## Example implementation

The chart described above can be implemented as follows. `Entry` and `chartEntryModelProducer` are defined in the first code sample in this document.

### Jetpack Compose

```kotlin
val bottomAxis = bottomAxis(
    valueFormatter = { value, chartValues ->
        (chartValues.chartEntryModel.entries[0].getOrNull(value.toInt()) as Entry?)
            ?.localDate
            ?.run { "$dayOfMonth/$monthValue" }
            .orEmpty()
    },
)

Chart(
    chart = columnChart(),
    chartModelProducer = chartEntryModelProducer,
    startAxis = startAxis(),
    bottomAxis = bottomAxis,
)
```

### View system

```xml
<com.patrykandpatrick.vico.views.chart.ChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chart="column"
    app:showBottomAxis="true"
    app:showStartAxis="true" />
```

```kotlin
with(findViewById<ChartView>(R.id.chart)) {
    entryProducer = chartEntryModelProducer
    (bottomAxis as HorizontalAxis).valueFormatter = AxisValueFormatter { value, chartValues ->
        (chartValues.chartEntryModel.entries[0].getOrNull(value.toInt()) as Entry?)
            ?.localDate
            ?.run { "$dayOfMonth/$monthValue" }
            .orEmpty()
    }
}
```
