This version includes the following changes.

## Additions

- chart legends
- data labels

## API changes

- In the `view` module, the `line*spec` attributes of `LineChartStyle` have been renamed to `line*Spec` (note the capitalization).
- `AxisValueFormatter` now inherits from `ValueFormatter`, which is used for data labels. The return type of `ValueFormatter#formatValue` is now `CharSequence` (as opposed to `String`).

## Resolved issues

- In line charts, points could be clipped near the top and bottom of the chart.
- There were mix-ups between `HorizontalPosition.Start` and `HorizontalPosition.End` as well as `VerticalPosition.Top` and `VerticalPosition.Bottom` in several places.

## Improvements

- In the `compose` module, the `text` function now uses `remember` to help prevent the unnecessary creation of new `TextComponent` instances.
