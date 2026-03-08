---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/android/core/cartesian-charts/cartesianchartmodelproducer
---

# CartesianChartModelProducer

A chart’s data is stored in its model, represented by [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/). Much like [`CartesianChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-chart/) combines [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/) instances, [`CartesianChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/) combines [`CartesianLayerModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-layer-model/) instances. Use the transaction-based [`CartesianChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/) to create models.

## Creation

Instantiate `CartesianChartModelProducer` via the constructor. Since data updates are performed via transactions, the producer used by a chart shouldn’t be replaced. Thus, store it in a place with sufficient persistence, such as a view model.

## `Transaction`

Use [`runTransaction`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/run-transaction) to perform an update in a transaction. This is a suspending function:

```kt
cartesianChartModelProducer.runTransaction { /* ... */ }
```

This function returns when the update is complete—that is, once a new model has been generated, and the host has been notified. If there’s already an update in progress, the current coroutine is first suspended until the ongoing update’s completion.

How data is added in a transaction depends on the layers in use, so we cover this later, on the pages that describe the individual [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.layer/-cartesian-layer/) implementations.

## Asynchrony

`runTransaction` works off the main thread, meaning that model generation and processing happen asynchronously. Moreover, during an update, two models may be dealt with at once—one in the foreground and one in the background. Thus, dynamic setup tied to model updates should be performed based on Vico-provided arguments of lambdas, interface functions, and the like. These functions may receive `CartesianChartModel` instances or related data. For such setup, external mechanisms should be avoided:

* Never directly use the data from the latest transaction.
* Never introduce a separate channel that makes setup changes as a transaction runs.

These solutions don’t have the tight coupling with the transaction mechanism that synchronization requires and may thus produce improper, unpredictable results.

For setup derived from series data, the correct, argument-based approach is straightforward, with the data being readily available in the model and its ranges. Functions may receive these directly, but they’re usually accessed via [`CartesianMeasuringContext`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-measuring-context/) and its subtypes; see [`CartesianMeasuringContext.model`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-measuring-context/model) and [`CartesianMeasuringContext.ranges`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian/-cartesian-measuring-context/ranges).

However, changes that aren’t directly derived from series data may also need to be aligned with model updates. We thus need a means of sending additional information through the same channel that’s used for series data. This is where extras, described in the following subsection, come in.

## Extras

Extras are a means of adding auxiliary data to models. They’re stored in [`CartesianChartModel.extraStore`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model/extra-store) and use typed keys ([`ExtraStore.Key`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.data/-extra-store/-key/) instances), enabling you to save any kind of data in a type-safe manner. To add extras, use [`Transaction.extras`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.cartesian.data/-cartesian-chart-model-producer/-transaction/extras), as shown below. (This is, of course, a simplified example. Extras are used for values that change; static values don’t require synchronization.)

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

Just like series data, extras can be accessed via function arguments. In [`ExtraStore`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.common.data/-extra-store/)-focused contexts, an `ExtraStore` instance is provided explicitly. Where this isn’t the case, use `CartesianChartModel.extraStore`, obtaining the model as described above. (That’s `context.model.extraStore` in most cases.)

Extras are read like `Map` elements. Assume you have an `ExtraStore` reference called `extraStore` and an `ExtraStore.Key` reference called `Key`. If the extra is added on every transaction, use the following:

```kt
extraStore[Key]
```

If the extra is added conditionally, meaning that it may not be present, use this:

```kt
extraStore.getOrNull(Key)
```

`ExtraStore.Key` instances are compared by identity. Given the asynchronous context in which they’re used, it’s important not to recreate or swap them more often than appropriate. Ensure the following:

* Each `ExtraStore.Key` instance used with a particular model producer is persisted at least as long as the corresponding `CartesianChartModelProducer` instance. (A universal approach is to store keys statically—at the top level, in companion objects, and so on. This can also make your code more elegant, eliminating the need for passing key references around.)
* A property’s `ExtraStore.Key` instance remains unchanged throughout the lifetime of the associated model producer.

See the following sample charts for examples of extra usage:

* [“AI test scores”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/AITestScores.kt)
* [“Daily digital-media use (USA)”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/DailyDigitalMediaUse.kt)
* [“Rock–metal ratios”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/RockMetalRatios.kt)

## Manual `CartesianChartModel` creation

Using `CartesianChartModelProducer` is recommended because it offers performance benefits and supports animations. However, you can create models manually via the `CartesianChartModel` constructor, which takes a list of `CartesianLayerModel` instances. When a host receives such a model, it handles it synchronously. Any asynchronous processing is explicitly handled by the consumer. Thus, extras are unneeded.

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
