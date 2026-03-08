---
metaLinks:
  alternates:
    - >-
      https://app.gitbook.com/s/Wpa2ykTaKZoySxzNtySN/multiplatform/cartesian-charts/cartesianmarker
---

# CartesianMarker

[`CartesianMarker`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker/) instances highlight points on charts. There are two types:

* Standard markers are shown on tap. Add these via the `marker` parameter of [`rememberCartesianChart`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian/remember-cartesian-chart).
* Persistent markers are shown permanently at given _x_-values. Add these via the `persistentMarkers` parameter of `rememberCartesianChart`.

There are two ways of creating `CartesianMarker` instances:

* using [`DefaultCartesianMarker`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-default-cartesian-marker/), described below
* implementing the interface

## `DefaultCartesianMarker`

A common means of creating `CartesianMarker` instances is [`DefaultCartesianMarker`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-default-cartesian-marker/), instantiated via [`rememberDefaultCartesianMarker`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/remember-default-cartesian-marker). It accepts three main components—a label, a point indicator, and a vertical line—and offers several customization options.

`DefaultCartesianMarker` label text is created by [`DefaultCartesianMarker.ValueFormatter`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-default-cartesian-marker/-value-formatter/). A dedicated formatting contract is required here because `CartesianMarker` instances can highlight multiple points at once. The general principle is the same as with [`CartesianValueFormatter`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.data/-cartesian-value-formatter/), but the required information must first be [retrieved from `targets`](cartesianmarker.md#cartesianmarkertarget).

A base `DefaultCartesianMarker.ValueFormatter` implementation can be instantiated via [`DefaultCartesianMarker.ValueFormatter.default`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-default-cartesian-marker/-value-formatter/-companion/default). This implementation uses `DecimalFormat` to format _y_-values. You can provide a custom `DecimalFormat` instance and toggle the color-coding of _y_-values. The former enables you not only to change how the _y_-values themselves are formatted, but also to add prefixes and suffixes.

`DefaultCartesianMarker` is open for subclassing.

## `CartesianMarkerVisibilityListener`

You can listen for visibility changes of standard markers via [`CartesianMarkerVisibilityListener`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker-visibility-listener/).

## `CartesianMarkerController`

By default, a standard marker appears on press and disappears on release. You can customize this with [`CartesianMarkerController`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker-controller/), which has built-in implementations instantiated via the following:

* [`CartesianMarkerController.rememberShowOnPress`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker-controller/-companion/remember-show-on-press) (default)
* [`CartesianMarkerController.rememberToggleOnTap`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker-controller/-companion/remember-toggle-on-tap)
* [`CartesianMarkerController.rememberShowOnHover`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker-controller/-companion/remember-show-on-hover)

For more specific behavior, create a custom implementation.

## `CartesianMarker.Target`

`CartesianMarker` instances use [`CartesianMarker.Target`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-cartesian-marker/-target/) instances, which hold information on the highlighted points. They are stored in lists (called `targets`), since several points may be highlighted at once.

`CartesianMarker.Target` itself includes only general properties. More data is found in its subtypes, each of which corresponds to a [`CartesianLayer`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.layer/-cartesian-layer/): [`CandlestickCartesianLayerMarkerTarget`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-candlestick-cartesian-layer-marker-target/), [`ColumnCartesianLayerMarkerTarget`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-column-cartesian-layer-marker-target/), and [`LineCartesianLayerMarkerTarget`](https://api.vico.patrykandpatrick.com/vico/compose/com.patrykandpatrick.vico.compose.cartesian.marker/-line-cartesian-layer-marker-target/).

In general, you should take the following approach when working with `targets`:

* If you know that only one point can be highlighted at once, use `targets.first()`. (This occurs when each point has a unique pixel _x_-coordinate—for example, when only a single-series `LineCartesianLayerMarkerTarget` is present.) Otherwise, iterate `targets`.
* If you know that only one kind of `CartesianMarker.Target` is present, use casting. (This occurs when only one kind of `CartesianLayer` is in use.) Otherwise, use type checking (`when` with `is`).
