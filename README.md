<figure>
    <img src="./cover.png" />
    <p align="center">
        These charts are implemented in <a href="https://github.com/patrykandpatryk/vico/tree/master/sample">the sample app</a>.
    </p>
</figure>

# Vico

![](https://img.shields.io/maven-central/v/com.patrykandpatryk.vico/core)
![](https://img.shields.io/github/workflow/status/patrykandpatryk/vico/Build%20debug%20APK)
![](https://www.codefactor.io/repository/github/patrykandpatryk/vico/badge)

Vico is a light and extensible chart library for Jetpack Compose and the view system. It comprises two main modules:

-   `compose` (for Jetpack Compose)
-   `view` (for views)

Vico has a very low number of dependecies, and these two modules don’t depend on each other. [Find out how.](#how-is-that)

## A foreword by the founder

As a library compatible with both Compose and views, Vico is quite unique. It doesn’t depend on the interoperability between the two UI systems.

The shared, main logic resides in the `core` module and depends on the Android SDK. It doesn’t know anything about views or Jetpack Compose. Likewise, `view` (for views) doesn’t know anything about Jetpack Compose, and `compose` (for Jetpack Compose) doesn’t know anything about views.

### Achieving module independence

I was a little curious about Jetpack Compose’s internals and how come it is interoperable with views.

“Can you, fairly easily, share the code used to draw on the `Canvas` between these two UI paradigms?” I asked myself.

The answer is yes. `core` uses `android.graphics.Canvas` (also used by views) to draw charts, and `androidx.compose.ui.graphics.drawscope.DrawScope` (used by Jetpack Compose) exposes `android.graphics.Canvas` via `DrawScope#canvas#nativeCanvas`. It’s similar for other APIs, like `Path`.

This approach encourages a greater level of abstraction and promotes separation of concerns. It also helped make the API highly extensible.

## Getting started

1. Ensure your app’s minimum SDK version is 16 (for `core` and `view`) or 21 (for `compose`, `compose-m2`, and `compose-m3`). This is declared in the module-level `build.gradle` file.

    ```groovy
    android {
        defaultConfig {
            minSdkVersion 16 // Or 21 for Jetpack Compose.
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
        implementation "com.patrykandpatryk.vico:view:1.0.0-beta.1"

        // Provides the utilities needed to use Vico in Jetpack Compose.
        implementation "com.patrykandpatryk.vico:compose:1.0.0-beta.1"

        // An optional addition for `vico.compose` that creates a `ChartStyle` based on an M2 Material Theme.
        implementation "com.patrykandpatryk.vico:compose-m2:1.0.0-beta.1"

        // An optional addition for `vico.compose` that creates a `ChartStyle` based on an M3 Material Theme.
        implementation "com.patrykandpatryk.vico:compose-m3:1.0.0-beta.1"

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

## Sample app

Included in this repository is [a sample app](https://github.com/patrykandpatryk/vico/tree/master/sample) with multiple charts and two tabs—one for Jetpack Compose, and the other one for the view system. Studying the source code of the app will give you a deep understanding of how to use Vico, including topics such as updating data and customizing charts. All of the charts included in the graphic at the top of this README are implemented in the sample app.

## Basic example

Chart data in Vico is stored in [`ChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model/)s. For a static chart, you can create a [`ChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model/) instance via the [`entryModelOf`](<https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/entry-model-of?query=fun%20entryModelOf(vararg%20entries:%20Pair%3CNumber,%20Number%3E):%20ChartEntryModel>) helper function:

```kt
val entryModel = entryModelOf(5f, 15f, 10f, 20f, 10f)
```

This creates a [`ChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model/) that can be used in, for example, a column chart. The chart will have five columns.

In Jetpack Compose, use the [`Chart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.chart/-chart) composable:

```kt
Chart(
    chart = columnChart(),
    model = entryModel,
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

In the view system, use [`ChartView`](https://patrykandpatryk.com/vico/api/vico/view/com.patrykandpatryk.vico.view.chart/-chart-view/):

```xml
<com.patrykandpatryk.vico.view.chart.ChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chart="column"
    app:showBottomAxis="true"
    app:showStartAxis="true" />
```

```kt
findViewById<ChartView>(R.id.chart).model = entryModel
```

## Dynamic data & data updates

For dynamic data sets, use [`ChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model-producer) (or [`ComposedChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry.composed/-composed-chart-entry-model-producer) for
[composed charts](#composed-charts)).

For this example, we’ll use a function that generates a random list of [`FloatEntry`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-float-entry/) instances. A [`FloatEntry`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-float-entry/) describes a single chart entry (e.g., a column).

```kt
fun getRandomEntries() = List(size = 5) {
    25f * Random.nextFloat()
}.mapIndexed { x, y ->
    FloatEntry(
        x = x.toFloat(),
        y = y,
    )
}
```

A list returned by this function can be used to initialize a [`ChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model-producer/), which should happen in the viewmodel:

```kt
val producer = ChartEntryModelProducer(getRandomEntries())
```

In Compose, you can once again use the [`Chart`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.chart/-chart) composable, but this time the overload with a `chartModelProducer` parameter:

```kt
Chart(
    chart = lineChart(),
    chartModelProducer = producer,
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

In the view system, you can connect a chart to a [`ChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model-producer) as follows:

```xml
<com.patrykandpatryk.vico.view.chart.ChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chartType="column"
    app:showBottomAxis="true"
    app:showStartAxis="true" />
```

```kt
findViewById<ChartView>(R.id.chart).entryProducer = producer
```

With [`ChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model-producer), you can update the data displayed by a chart. For the above example, this would be done as follows:

```kt
chartModelProducer.setEntries(getRandomEntries()
```

Differences are animated by default.

## Composed charts

You can combine multiple charts into one. This example uses the `getRandomEntries` function from above.

A composed chart requires a [`ComposedChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.chart.composed/-composed-chart-entry-model/?query=interface%20ComposedChartEntryModel%3CModel%20:%20ChartEntryModel%3E%20:%20ChartEntryModel).
In the case of static charts, you can directly create a [`ComposedChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.chart.composed/-composed-chart-entry-model/?query=interface%20ComposedChartEntryModel%3CModel%20:%20ChartEntryModel%3E%20:%20ChartEntryModel) instance via the [`composedChartEntryModelOf`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry.composed/-composed-chart-entry-model-producer/-companion/composed-chart-entry-model-of) helper function.
You can also use the [`+`](https://patrykandpatryk.com/vico/api/vico/compose/com.patrykandpatryk.vico.compose.chart.entry/plus?query=operator%20fun%20%3CModel%20:%20ChartEntryModel%3E%20Model.plus(other:%20Model):%20ComposedChartEntryModel%3CModel%3E) operator to create a [`ComposedChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.chart.composed/-composed-chart-entry-model/?query=interface%20ComposedChartEntryModel%3CModel%20:%20ChartEntryModel%3E%20:%20ChartEntryModel) out of two [`ChartEntryModel`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model/)s.
For dynamic composed charts, use [`ComposedChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry.composed/-composed-chart-entry-model-producer).

For this example, weʼll use a [`ComposedChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry.composed/-composed-chart-entry-model-producer). You can create a [`ComposedChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry.composed/-composed-chart-entry-model-producer) out of two regular [`ChartEntryModelProducer`](https://patrykandpatryk.com/vico/api/vico/core/com.patrykandpatryk.vico.core.entry/-chart-entry-model-producer)s:

```kt
val firstProducer = ChartEntryModelProducer(getRandomEntries())
val secondProducer = ChartEntryModelProducer(getRandomEntries())
val composedProducer = firstProducer + secondProducer
```

A composed chart can be created in Jetpack Compose as follows:

```kt
Chart(
    chart = lineChart() + columnChart(),
    chartModelProducer = composedProducer,
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

And here’s the same chart in the view system. Note that we’re now using [`ComposedChartView`](https://patrykandpatryk.com/vico/api/vico/view/com.patrykandpatryk.vico.view.chart/-composed-chart-view/):

```xml
<com.patrykandpatryk.vico.view.chart.ComposedChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chartType="column"
    app:showBottomAxis="true"
    app:showStartAxis="true"
    app:charts="line|column" />
```

```kt
findViewById<ComposedChartView>(R.id.chart).entryProducer = composedProducer
```

## Further reading

Vico offers rich options for customization and an extensible API. The following resources are available to help you learn about it:

- [The sample app](https://github.com/patrykandpatryk/vico/tree/master/sample) contains a full implementation of Vico.
- [The wiki](https://patrykandpatryk/vico/wiki) describes core topics and includes a detailed customization guide.
- [The API reference](https://patrykandpatryk.com/vico/api) describes every public class, function, and field.
