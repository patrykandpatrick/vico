*Note: While Vico is already available, the README and wiki are under construction.*

# Vico

Vico is a light and extensible chart library for Jetpack Compose and the view system. It comprises two main modules:

-   `vico.compose` (for Jetpack Compose)
-   `vico.view` (for views)

Vico has a very low number of dependecies, and these two modules don’t depend on each other. [Find out how.](#how-is-that)

## A message from the founder

As a library compatible with both Compose and views, Vico is quite unique. It doesn’t depend on the interoperability between the two UI systems.

The shared, main logic resides in the `vico.core` module and depends on the Android SDK. It doesn’t know anything about views or Jetpack Compose. Likewise, `vico.view` (for views) doesn’t know anything about Jetpack Compose, and `vico.compose` (for Jetpack Compose) doesn’t know anything about views.

### How is that?

I was a little curious about Jetpack Compose’s internals and how come it is interoperable with views.

“Can you, fairly easily, share the code used to draw on the `Canvas` between these two UI paradigms?” I asked myself.

The answer is yes. `vico.core` uses `android.graphics.Canvas` (also used by views) to draw charts, and `androidx.compose.ui.graphics.drawscope.DrawScope` (used by Jetpack Compose) exposes `android.graphics.Canvas` via `DrawScope#canvas#nativeCanvas`. It’s similar for other APIs, like `Path`.

This approach encourages a greater level of abstraction and promotes separation of concerns. It also helped make the API highly extensible.

## Getting started

1. Ensure your app’s minimum SDK version is 16 (for `vico.core` and `vico.view`) or 21 (for `vico.compose`, `vico.compose-m2`, and `vico.compose-m3`). This is declared in the module-level `build.gradle` file.

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

        // Provides the utilities needed to use Vico in the view system.
        implementation "com.patrykandpatryk.vico:view:1.0.0-alpha.1"

        // Provides the utilities needed to use Vico in Jetpack Compose.
        implementation "com.patrykandpatryk.vico:compose:1.0.0-alpha.1"

        // An optional addition for `vico.compose` that creates a `ChartStyle` based on an M2 Material Theme.
        implementation "com.patrykandpatryk.vico:compose-m2:1.0.0-alpha.1"

        // An optional addition for `vico.compose` that creates a `ChartStyle` based on an M3 Material Theme.
        implementation "com.patrykandpatryk.vico:compose-m3:1.0.0-alpha.1"

        ...
    }
    ```

## Modules

The following table outlines the modules included in this library:

| Artifact     | Description                                                                                   |
| ------------ | --------------------------------------------------------------------------------------------- |
| `core`       | Includes the core logic for charts and other components. All modules depend on `core`.        |
| `view`       | Provides the utilities needed to use Vico in the view system.                                 |
| `compose`    | Provides the utilities needed to use Vico in Jetpack Compose.                                 |
| `compose-m2` | An optional addition for `compose` that creates a `ChartStyle` based on an M2 Material Theme. |
| `compose-m3` | An optional addition for `compose` that creates a `ChartStyle` based on an M3 Material Theme. |

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

A basic column chart with two axes and five entries can be added as follows. First, add a `ChartView` to your XML layout file:

```xml
<com.patrykandpatryk.vico.view.chart.ChartView
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
