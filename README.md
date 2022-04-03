# Vico Chart

## Getting started

1. Ensure your app’s minimum SDK version is 16 (for `vico.core` and `vico.view`) or 21 (for`vico.compose`, `vico.compose-m2`, and `vico.compose-m3`). This is declared in the module-level `build.gradle` file.
    ```groovy
    android {
        defaultConfig {
            minSdkVersion 21
            ...
        }
        ...
    }
    ```
1. Ensure the `mavenCentral()` repository is declared in the project-level `build.gradle` file:
    ```groovy
    buildscript {
        repositories {
            mavenCentral()
            ...
        }
        ...
    }
    ```
1. Declare the dependencies you need in the module-level `build.gradle` file. All modules depend on `vico.core`, so you don’t need to add it as a dependency.
    ```groovy
    dependencies {
        implementation "com.patrykandpatryk.vico:vico-compose:1.0.0-alpha.1"
        implementation "com.patrykandpatryk.vico:vico-compose-m3:1.0.0-alpha.1"
        ...
    }
    ```

## Modules

The following table outlines the modules included in this library:

| Group             | Description                                                                                 |
| ----------------- | ------------------------------------------------------------------------------------------- |
| `vico.compose`    | Provides the utilities needed to use Vico in Jetpack Compose.                               |
| `vico.compose-m2` | Helps create a `ChartStyle` based on an M2 Material Theme.                                  |
| `vico.compose-m3` | Helps create a `ChartStyle` based on an M3 Material Theme.                                  |
| `vico.core`       | Includes the core logic for charts and other components. All modules depend on `vico.core`. |
| `vico.view`       | Provides the utilities needed to use Vico in the view system.                               |

## Jetpack Compose

### Basic example

A basic column chart with two axes and five entries can be added as follows:

```kt
Chart(
    chart = columnChart(),
    model = entryModelOf(5f, 15f, 10f, 20f, 10f),
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

### `ChartEntryModelProducer` & updating data

For more complex datasets, use `ChartEntryModelProducer` (or `ComposedChartEntryModelProducer` for
[composed charts](#composed-charts)). The following example displays a line chart and a button that updates its data:

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

### Composed charts

You can combine multiple charts into one. The following example displays a composed chart that
comprises a column chart and a line chart, each displaying a different randomized dataset:

```kt
fun getRandomEntries() = List(size = 5) {
    25f * Random.nextFloat()
}.mapIndexed { x, y ->
    FloatEntry(
        x = x.toFloat(),
        y = y,
    )
}

val firstChartModelProducer = remember { ChartEntryModelProducer(getRandomEntries()) }
val secondChartModelProducer = remember { ChartEntryModelProducer(getRandomEntries()) }
val composedChartModelProducer = remember(
    firstChartModelProducer,
    secondChartModelProducer,
) { firstChartModelProducer + secondChartModelProducer }

Chart(
    chart = lineChart() + columnChart(),
    chartModelProducer = chartModelProducer,
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

### Customization

The easiest way to customize the appearance of charts is to provide a custom `ChartStyle`
implementation via `ProvideChartStyle`. This enables you to customize the colors, dimensions,
typography, and more. All charts and their components use the style provided by `ProvideChartStyle`
by default, but you can customize each chart individually if needed. For example, the `lineChart`
function allows you to specify the line thickness, the line color, and so on. The default values
for these parameters are `currentChartStyle.lineChart.lineThickness` and
`currentChartStyle.lineChart.lineColor`. `ProvideChartStyle` is similar to `MaterialTheme` from
`compose.material` and `compose.material3`. It can be used as follows:

```kt
ProvideChartStyle(chartStyle = ChartStyle(...)) {
   ...
}
```

If you use `compose.material` or `compose.material3` in your app, you can use the `m2ChartStyle`
(`vico.compose-m2`) or `m3ChartStyle` (`vico.compose-m3`) function to create a `ChartStyle`
implementation that uses the colors from your app’s Material Theme:

```kt
ProvideChartStyle(chartStyle = m3ChartStyle()) {
    ...
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
