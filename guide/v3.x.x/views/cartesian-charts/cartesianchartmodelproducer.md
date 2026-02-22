---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianchartmodelproducer
---

# CartesianChartModelProducer

## Overview

A [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/)’s data is stored in its [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/). Much like a `CartesianChart` is a collection of `CartesianLayer`s, a `CartesianChartModel` is a collection of [`CartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-model/)s. `CartesianChartModel`s are created via the [`Transaction`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/-transaction/)-based [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/).

## `CartesianChartModelProducer` creation

Create a `CartesianChartModelProducer` via the constructor. A `CartesianChart`’s `CartesianChartModelProducer` mustn’t be replaced—data updates are performed via `Transaction`s—so store the `CartesianChartModelProducer` in a place with sufficient persistence, such as a `ViewModel`.

## `Transaction`

`Transaction`s are run via [`runTransaction`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/run-transaction), which is a suspending function:

```kt
cartesianChartModelProducer.runTransaction { /* ... */ }
```

This function returns when the update is complete—that is, once a new `CartesianChartModel` has been generated, and the hosts have been notified. If there’s already an update in progress, the current coroutine is first suspended until the ongoing update’s completion.

How data is added to a `Transaction` depends on the `CartesianLayer`s in use, so we cover this later, on the pages that describe the individual `CartesianLayer`s.

## Asynchrony

`Transaction`s are handled off the main thread, meaning that `CartesianChartModel`s are generated and processed asynchronously. Moreover, during an update, two `CartesianChartModel`s may be being dealt with at once—one in the foreground and one in the background. Thus, dynamic setup tied to `CartesianChartModel` updates based on Vico-provided arguments of lambdas, interface functions, and the like. These functions may receive `CartesianChartModel`s themselves or related data. For such setup, external mechanisms should be avoided:

* Never directly use the data from the latest `Transaction`.
* Never introduce a separate channel that makes setup changes as `Transaction`s are run.

These solutions don’t have the tight coupling with the `Transaction` mechanism that is required for synchronization and may thus produce improper, unpredictable results.

For setup derived from series data, the correct, argument-based approach is straightforward, with the data being readily available in `CartesianChartModel` and [`CartesianChartRanges`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-ranges/). Functions may receive these directly, but they’re usually accessed via `CartesianMeasuringContext` and subtypes thereof; see [`CartesianMeasuringContext.model`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-measuring-context/model) and [`CartesianMeasuringContext.ranges`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-measuring-context/ranges).

However, changes that aren’t directly derived from series data may also need to be aligned with `CartesianChartModel` updates. We thus need a means of sending additional information through the same channel that’s used for series data. This is where extras, described in the following subsection, come in.

## Extras

Extras are a means of adding auxiliary data to `CartesianChartModel`s. They’re stored in [`CartesianChartModel.extraStore`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/extra-store) and have typed keys ([`ExtraStore.Key`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.data/-extra-store/-key/) instances), enabling you to save any kind of data in a type-safe manner. To add extras, use [`Transaction.extras`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/-transaction/extras), as shown below. (This is, of course, a simplified example. Extras are used for values that change; static values don’t require synchronization.)

```kt
val UnitKey = ExtraStore.Key<String>()
```

```kt
cartesianChartModelProducer.runTransaction {
    extras { extraStore ->
        extraStore[UnitKey] = "Ω"
        // ...
    }
    // ...
}
```

Just like series data, extras can be accessed via function arguments. In [`ExtraStore`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.data/-extra-store/)-focused contexts, the `ExtraStore` is provided explicitly. Where this isn’t the case, use `CartesianChartModel.extraStore`, obtaining the `CartesianChartModel` as described in above. (That’s `context.model.extraStore` in most cases.)

Extras are read like `Map` elements. Assume you have an `ExtraStore` reference called `extraStore` and an `ExtraStore.Key` called `Key`. If the extra is added on every `Transaction`, use the following:

```kt
extraStore[Key]
```

If the extra is added conditionally, meaning that it may not be present, use this:

```kt
extraStore.getOrNull(Key)
```

`ExtraStore.Key`s are compared by instance. Given the asynchronous context in which they’re used, it’s important not to recreate or swap them more often than appropriate. Ensure the following:

* All `ExtraStore.Key` instances used with a particular `CartesianChartModelProducer` are persisted at least as long as the `CartesianChartModelProducer`. (A universal approach is to store `ExtraStore.Key` statically—at the top level, in companion objects, and so on. This can also make your code more elegant, eliminating the need for passing `ExtraStore.Key`s around.)
* Any given property’s `ExtraStore.Key` remains unchanged throughout the lifetime of the associated `CartesianChartModelProducer`.

See the following sample charts for examples of extra usage:

* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)

## Manual `CartesianChartModel` creation

`CartesianChartModelProducer` is recommended because it offers performance benefits and supports animations. However, you can create `CartesianChartModel`s manually via the constructor, which takes a list of `CartesianLayerModel`s. When a host receives a `CartesianChartModel`, it handles it synchronously. Any asynchronous processing is explicitly handled by the consumer. Thus, extras are unneeded.

## Sample charts

* [“Basic column chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicColumnChart.kt)
* [“Basic line chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicLineChart.kt)
* [“Basic combo chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicComboChart.kt)
* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Temperature anomalies (June)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/TemperatureAnomalies.kt)
* [“Electric-car sales (Norway)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/ElectricCarSales.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)
* [“Gold prices (12/30/2024)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/GoldPrices.kt)
