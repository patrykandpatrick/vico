# CartesianChartView

[`CartesianChartView`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart-view/) is the entry point for Cartesian charts in the view system. It accepts [`CartesianChart`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/) and [`CartesianChartModelProducer`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/) instances, facilitates communication between them, and displays the chart. It also handles scroll and zoom and shows a placeholder when no data is available.

`CartesianChartView` automatically creates a chart when the `layers` attribute is used:

```xml
<style name="ChartStyle">
    <item name="layers"><!-- ... --></item>
    <!-- ... -->
</style>
```

```xml
<com.patrykandpatrick.vico.views.cartesian.CartesianChartView
    app:chartStyle="@style/ChartStyle"
    <!-- ... --> />
```

Use the [`CartesianChartView.chart`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart-view/chart) property to get a reference to this chart. Also use it to apply a chart created programmatically:

```kt
cartesianChartView.chart = CartesianChart(/* ... */)
```

## Data

Use [`CartesianChartView.modelProducer`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart-view/model-producer) to set a [`CartesianChartModelProducer`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/) instance:

```kt
cartesianChartView.modelProducer = cartesianChartModelProducer
```

You can also use a manually created [`CartesianChartModel`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/) instance via the [`model`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart-view/model) property:

```kt
cartesianChartView.model = model
```

## Multiple layers

You can add multiple layers by separating their names with pipes:

```xml
<style name="ChartStyle">
    <item name="layers">column|line</item>
    <!-- ... -->
</style>
```

```xml
<com.patrykandpatrick.vico.views.cartesian.CartesianChartView
    app:chartStyle="@style/ChartStyle"
    <!-- ... --> />
```

## Scroll and zoom

There are XML attributes for toggling scroll and zoom:

```xml
<style name="ChartStyle">
    <item name="scrollEnabled">true</item>
    <item name="zoomEnabled">true</item>
    <!-- ... -->
</style>
```

```xml
<com.patrykandpatrick.vico.views.cartesian.CartesianChartView
    app:chartStyle="@style/ChartStyle"
    <!-- ... --> />
```

More advanced customization uses [`ScrollHandler`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll-handler/) and [`ZoomHandler`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom-handler/):

```kt
val scrollHandler = ScrollHandler(/* ... */)
val zoomHandler = ZoomHandler(/* ... */)
```

```kt
cartesianChartView.scrollHandler = scrollHandler
cartesianChartView.zoomHandler = zoomHandler
```

You can also toggle [`ScrollHandler.scrollEnabled`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll-handler/scroll-enabled/) directly. 

[`Scroll`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/) represents scroll values—either absolute (from zero) or relative (from the current value). [`Scroll.Absolute`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/) and [`Scroll.Relative`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/) cover the two cases. The following factory functions are available:

* [`Scroll.Absolute.pixels`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/-companion/pixels)
* [`Scroll.Absolute.x`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-absolute/-companion/x)
* [`Scroll.Relative.pixels`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/-companion/pixels)
* [`Scroll.Relative.x`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-scroll/-relative/-companion/x)

[`AutoScrollCondition`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/) defines when to scroll automatically. There are two singletons:

* [`AutoScrollCondition.Never`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/-companion/-never) (default)
* [`AutoScrollCondition.OnModelGrowth`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-auto-scroll-condition/-companion/-on-model-growth)

[`Zoom`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/) defines zoom factors. The following singletons and factory functions are available:

* [`Zoom.Content`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/-content)
* [`Zoom.max`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/max)
* [`Zoom.min`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/min)
* [`Zoom.fixed`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/fixed)
* [`Zoom.x`](https://views.api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-zoom/-companion/x)

While the built-in implementations cover the majority of use cases, you can create custom implementations of `Scroll`, `AutoScrollCondition`, and `Zoom` for more advanced behavior.

## Sample charts

* [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicColumnChart.kt)
* [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicLineChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicComboChart.kt)
* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/TemperatureAnomalies.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/ElectricCarSales.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)
* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/views/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/GoldPrices.kt)
