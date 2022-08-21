This release includes the following changes.

## Resolved issues

- The `textComponent` composable function was missing a `typeface` parameter.
- The `LineChart.LineSpec#copy` function was missing the following parameters: `dataLabel`, `dataLabelVerticalPosition`, `dataLabelValueFormatter`, and `dataLabelRotationDegrees`.
- `HorizontalAxis` could draw ticks, guidelines, and labels incorrectly for some line charts.
- Charts with only one entry were rendered incorrectly and could cause crashes.

## Dependency updates

Please see [this commit](https://github.com/patrykandpatryk/vico/commit/099ab29fd97b188ef8056761bc7e4bf04f1dff41) for information on the dependency updates included in this release.
