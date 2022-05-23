This version includes the following changes.

## API Changes

- In the `view` module, the `line*spec` attributes of `LineChartStyle` have been renamed to `line*Spec` (note the capitalization).

## Improvements

- In the `compose` module, the `text` function now uses `remember` to help prevent the unnecessary creation of new `TextComponent` instances.
