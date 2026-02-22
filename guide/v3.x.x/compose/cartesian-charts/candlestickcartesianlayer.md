---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/candlestickcartesianlayer
---

# CandlestickCartesianLayer

## Overview

Use [`CandlestickCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-candlestick-cartesian-layer/) to create candlestick charts. Instantiate `CandlestickCartesianLayer` via [`rememberCandlestickCartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/remember-candlestick-cartesian-layer).

Each candle’s style is defined by its corresponding [`Candle`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-candlestick-cartesian-layer/-candle/). The `Candle`s are provided by a [`CandleProvider`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-candlestick-cartesian-layer/-candle-provider/):

* To style candles based on their absolute price changes (closing vs. opening), use [`absolute`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/absolute). This is commonly used for filled candles and has such defaults.
* To style candles based on both their absolute price changes (closing vs. opening) and their relative price changes (closing vs. previous closing), use [`absoluteRelative`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/absolute-relative). This is commonly used for hollow candles and has such defaults.
* For custom behavior, implement `CandleProvider`.

At the `rememberCandlestickCartesianLayer` level, you can set the minimum body height, change the candle spacing, and toggle wick scaling.

## `Transaction.candlestickSeries`

`CandlestickCartesianLayer`s use [`CandlestickCartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-candlestick-cartesian-layer-model/)s. When using a [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model-producer/), add `CandlestickCartesianLayerModel`s via [`candlestickSeries`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/candlestick-series):

```kt
cartesianChartModelProducer.runTransaction {
    candlestickSeries(
        x = listOf(1, 2, 3, 4),
        opening = listOf(2, 4, 6, 3),
        closing = listOf(4, 5, 3, 3),
        low = listOf(1, 4, 2, 2),
        high = listOf(5, 6, 7, 4),
    )
    // ...
}
```

`candlestickSeries` also has an overload with no `x` parameter, which uses the indices of the prices as the _x_-values:

```kt
candlestickSeries(
    opening = listOf(2, 4, 6, 3),
    closing = listOf(4, 5, 3, 3),
    low = listOf(1, 4, 2, 2),
    high = listOf(5, 6, 7, 4),
)
```

## Manual `CandlestickCartesianLayerModel` creation

When creating a [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-chart-model/) directly, you can add a `CandlestickCartesianLayerModel` by using [`build`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-candlestick-cartesian-layer-model/-companion/build):

```kt
CartesianChartModel(
    ColumnCartesianLayerModel.build(
        x = listOf(1, 2, 3, 4),
        opening = listOf(2, 4, 6, 3),
        closing = listOf(4, 5, 3, 3),
        low = listOf(1, 4, 2, 2),
        high = listOf(5, 6, 7, 4),
    ),
    // ...
)
```

This function also has an overload with no `x` parameter:

```kt
ColumnCartesianLayerModel.build(
    opening = listOf(2, 4, 6, 3),
    closing = listOf(4, 5, 3, 3),
    low = listOf(1, 4, 2, 2),
    high = listOf(5, 6, 7, 4),
)
```

## Sample charts

* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/compose/src/commonMain/kotlin/com/patrykandpatrick/vico/sample/charts/compose/GoldPrices.kt)
