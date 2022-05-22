## Additions

- `ColumnChart` and `LineChart` instances can now define a `targetVerticalAxisPosition`. This property allows for the charts and vertical axes in a `ComposedChart` to be independently scaled (#73).

- A single `LineChart` can now display multiple lines when supplied with a `ChartEntryModel` containing multiple datasets.

- The `view` module has new `lineChart()` and `columnChart()` functions that create `LineChart`s  and `ColumnChart`s, respectively, using theme and style attributes.


## Resolved issues

- During the measuring phase, `HorizontalAxis` didn’t provide the `TextComponent` instance used for axis labels with a maximum width. This could lead to faulty truncation behavior (#77).

- `TextComponent` instances with a custom rotation value and a maximum width could display faulty truncation behavior (#77).

- Persistent markers added to a `Chart` contained in a `ComposedChart` were clipped.

## API changes

- `ChartModel` has been renamed to `ChartValues` for clarity.

- `LineChart`’s constructors now accept a single `LineChart.LineSpec` or a `List` of `LineChart.LineSpec`s. `LineChart.LineSpec` defines the style of a single line in a line chart. See also the second point in “Additions.”

- Changes have been made to how line charts are customized via `ChartStyle` to account for the addition of the ability to display multiple lines in a single `LineChart`. See also the second point in “API changes” and the second point in “Additions.”

## Dependency updates

- All modules use Kotlin 1.6.21.

- `compose`, `compose-m2`, and `compose-m3` use Jetpack Compose version `1.2.0-beta02`.

- `compose-m3` uses `compose.material3` version `1.0.0-alpha12`.
