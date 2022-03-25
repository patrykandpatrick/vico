# Vico Chart

## Getting started

## Modules
| Group | Description |
| --- | --- |
| `vico.compose` | TBD |
| `vico.compose-m2` | TBD |
| `vico.compose-m3` | TBD |
| `vico.core` | TBD |
| `vico.view` | TBD |

## Jetpack Compose
A basic column chart with two axes and five entries can be added as follows:
```kt
Chart(
    chart = columnChart(),
    model = entryModelOf(5f, 15f, 10f, 20f, 10f),
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```
For more complex datasets, use `ChartEntryModelProducer` (or `ComposedChartEntryModelProducer` for
composed charts). The following example displays a line chart and a button that updates its data:
```kt
fun getRandomEntries() = List(size = 5) {
    25f * Random.nextFloat()
}.mapIndexed { x, y ->
    FloatEntry(
        x = x.toFloat(),
        y = y,
    )
}

val chartModelProducer = remember { ChartEntryModelProducer(getRandomEntries()) }

Column {
    Chart(
        chart = lineChart(),
        chartModelProducer = chartModelProducer,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
    )
    Spacer(modifier = Modifier.height(16.dp))
    Button(onClick = { chartModelProducer.setEntries(getRandomEntries()) }) {
        Text(text = "Update entries")
    }
}
```

## Views
A basic column chart with two axes and five entries can be added as follows. First, add a
`ChartView` to your XML layout file:
```xml
<pl.patrykgoworowski.vico.view.chart.ChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chartType="column"
    app:showBottomAxis="true"
    app:showStartAxis="true" />
```
Then, set the model for the chart:
```kt
findViewById<ChartView>(R.id.chart).model = entryModelOf(5f, 15f, 10f, 20f, 10f)
```
