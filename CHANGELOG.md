# Additions

- A `composedChartEntryModelOf` function for creating `ComposedChartEntryModel`s.
- A `ChartEntryModel#plus` function for creating `ComposedChartEntryModel`s.

# Improvements

- `verticalGradient` is no longer a composable function.

# Resolved issues

- Charts were drawn incorrectly if the layout direction was right-to-left.
- Charts created via the `Chart` composable werenʼt scrollable if `marker` was null.
- Line charts were drawn incorrectly if `minY` or `maxY` was overridden.
- In line charts, the line was clipped near the top and bottom of the chart.
- The `strokeColor` parameter of the `ShapeComponent` constructor wasnʼt respected.
- Marker guidelines could be off-center relative to their corresponding chart entries.
- In `DashedShape`, if `dashLengthDp` and `dashGapDp` were both set to `0f`, an infinite loop would occur.
