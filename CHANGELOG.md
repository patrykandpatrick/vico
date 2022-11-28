This release includes the following changes.

## Additions

- Fading edges are now available. This feature applies a horizontal fade to the edges of the chart area for scrollable charts.
- You can now turn off the automatic scaling up of charts that, at a scale factor of 1, wouldn’t fill up the entire available horizontal area.

## Improvements

- In the `compose` module, all functions that create components are now composable and use `remember`. This is to improve performance.

## API changes

- In the `compose` module, two duplicate functions have been deprecated. Additionally, several functions have been moved, with the old functions having been deprecated.

## Resolved issues

- The `Chart` composable failed to respond to model updates after being recomposed with a new `ChartModelProducer`.
- `TextComponent` mispositioned its background when `textAlign` was set to `Paint.Align.CENTER` or `Paint.Align.RIGHT`.
