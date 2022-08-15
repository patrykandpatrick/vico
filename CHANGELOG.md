## Additions

- `LineChart` now allows you to specify the horizontal position of each point in its corresponding segment. When `PointPosition.Start` is used, there’s no gap between the left edge of the chart and the line’s first point.
- `LineChart.LineSpec` now allows for granular control of the line’s shape via `LineChart.LineSpec.PointConnector`. See also the first point under “API changes.”
- You can now disable the initial animation for charts. This is possible via the `runInitialAnimation` parameter of the `Chart` composable, and the `runInitialAnimation` field of `BaseChartView`.

## API changes

- The `cubicStrength` parameter of the `LineChart.LineSpec` constructor is deprecated. The same applies to the `cubicStrength` parameter of the `lineSpec` composable function. Instead, provide a `LineChart.LineSpec.PointConnector` with a custom `cubicStrength` value. See also the second point under “Additions.”
- `ChartModelProducer#registerForUpdates` now has a `getOldModel` parameter. See also the third point under “Resolved issues.”
- `ChartModelProducer` now has an `isRegistered` function.

## Resolved issues

- `MarkerCorneredShape` could trigger a crash in certain instances.
- `ColumnChart` could create redundant touch detection areas for markers.
- `ChartEntryModelProducer#registerForUpdates` called `updateListener` before `DiffProcessor#setEntries` and called `progressModelSynchronously` with `progress` equal to zero after calling `updateListener`. This could cause issues during chart creation. See also the second point under “API changes.”
- `BaseChartView` didn’t call `ChartModelProducer#unregisterFromUpdates` when detached from its window. This could potentially cause memory leaks.
