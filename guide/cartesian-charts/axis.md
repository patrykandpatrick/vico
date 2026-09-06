---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/axis
---

# Axis

[`Axis`][axis] draws an axis along an edge of a [`CartesianChart`][cartesian-chart] instance. There are two built-in implementations: [`HorizontalAxis`][horizontal-axis] and [`VerticalAxis`][vertical-axis]. Use [`VerticalAxis.rememberStart`][vertical-axis-remember-start], [`HorizontalAxis.rememberTop`][horizontal-axis-remember-top], [`VerticalAxis.rememberEnd`][vertical-axis-remember-end], and [`HorizontalAxis.rememberBottom`][horizontal-axis-remember-bottom] to instantiate these:

```kt
rememberCartesianChart(
    startAxis = VerticalAxis.rememberStart(/* ... */),
    topAxis = HorizontalAxis.rememberTop(/* ... */),
    endAxis = VerticalAxis.rememberEnd(/* ... */),
    bottomAxis = HorizontalAxis.rememberBottom(/* ... */),
    // ...
)
```

Numerous customization options are available; you can change the appearance of the labels, modify the axis lines, add titles, and more.

[`BaseAxis.TickPosition`][base-axis-tick-position] lets you place ticks outside, inside, or across the axis line.

## Drawing order

[`CartesianChart.DrawingOrder`][cartesian-chart-drawing-order] defines where content is drawn relative to the chart layers. There are three positions:

* [`UnderLayers`][under-layers] draws the content under the layers.
* [`OverAreaFills`][over-area-fills] draws the content over the layers’ area fills and under the rest of their content—strokes, points, and data labels.
* [`OverLayers`][over-layers] draws the content over the layers.

An axis has two independent positions: `lineDrawingOrder`, for the ticks and the axis line, and `guidelineDrawingOrder`, for the guidelines. [`Decoration`][decoration] instances take the same positions via `drawingOrder`.

[`OverAreaFills`][over-area-fills] addresses the case where a [`LineCartesianLayer`][line-cartesian-layer] instance with an [`AreaFill`][area-fill] would otherwise hide the guidelines. It requires the layer to draw its area fills separately, which [`LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst`][area-fills-first] enables:

```kt
rememberCartesianChart(
    rememberLineCartesianLayer(
        seriesDrawingOrder = LineCartesianLayer.SeriesDrawingOrder.AreaFillsFirst,
        // ...
    ),
    startAxis =
        VerticalAxis.rememberStart(
            guidelineDrawingOrder = CartesianChart.DrawingOrder.OverAreaFills
        ),
    // ...
)
```

[`AreaFillsFirst`][area-fills-first] is useful on its own too: it draws every series’ area fill before any series’ stroke, so no area fill covers a stroke. Layers that don’t draw their area fills separately—a [`ColumnCartesianLayer`][column-cartesian-layer] instance, which has none—are drawn entirely over content at this position, and keep their position relative to the other layers.

## Titles

[`BaseAxis.TitlePosition`][base-axis-title-position] defines where an axis title is drawn. There are two options:

* [`Side`][side] uses the conventional position beside the axis.
* [`End`][end] places the title at the leading end of the axis line.

Select an option via the `titlePosition` parameter.

## `ItemPlacer`

[`HorizontalAxis.ItemPlacer`][horizontal-axis-item-placer] and [`VerticalAxis.ItemPlacer`][vertical-axis-item-placer] let you customize for what _x_- and _y_-values labels and lines are displayed. Four factory functions are available:

* [`HorizontalAxis.ItemPlacer.aligned`][horizontal-axis-item-placer-aligned] (default)
* [`HorizontalAxis.ItemPlacer.segmented`][horizontal-axis-item-placer-segmented]
* [`VerticalAxis.ItemPlacer.step`][vertical-axis-item-placer-step] (default)
* [`VerticalAxis.ItemPlacer.count`][vertical-axis-item-placer-count]

Custom implementations can be created.

By default, `HorizontalAxis.ItemPlacer.aligned` reserves horizontal margins so the extreme labels fit. Set `shiftExtremeLabels` to `true` to anchor these labels to the coordinate-system edges instead, keeping them visible without shrinking the coordinate system.

[axis]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-axis/
[cartesian-chart]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/
[horizontal-axis]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/
[vertical-axis]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/
[vertical-axis-remember-start]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-companion/remember-start.html
[horizontal-axis-remember-top]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-companion/remember-top.html
[vertical-axis-remember-end]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-companion/remember-end.html
[horizontal-axis-remember-bottom]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-companion/remember-bottom.html
[base-axis-tick-position]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-base-axis/-tick-position/
[cartesian-chart-drawing-order]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/-drawing-order/
[under-layers]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/-drawing-order/-under-layers/
[over-area-fills]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/-drawing-order/-over-area-fills/
[over-layers]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/-cartesian-chart/-drawing-order/-over-layers/
[decoration]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.decoration/-decoration/
[line-cartesian-layer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/
[area-fill]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-area-fill/
[area-fills-first]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-line-cartesian-layer/-series-drawing-order/-area-fills-first/
[column-cartesian-layer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-column-cartesian-layer/
[base-axis-title-position]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-base-axis/-title-position/
[side]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-base-axis/-title-position/-side/
[end]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-base-axis/-title-position/-end/
[horizontal-axis-item-placer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/
[vertical-axis-item-placer]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/
[horizontal-axis-item-placer-aligned]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/-companion/aligned.html
[horizontal-axis-item-placer-segmented]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-horizontal-axis/-item-placer/-companion/segmented.html
[vertical-axis-item-placer-step]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/-companion/step.html
[vertical-axis-item-placer-count]: https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.axis/-vertical-axis/-item-placer/-companion/count.html
