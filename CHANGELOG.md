## Additions
- `ColumnChart` and `LineChart` can now define `targetVerticalAxisPosition`. The property allows
having different values displayed in vertical axes when charts are put into `ComposedChart`. #73
- A single `LineChart` can now display multiple lines when supplied with `ChartEntryModel` 
containing multiple sets of data.
- The `view` module has new `lineChart()` and `columnChart()` functions that create a `LineChart` 
and `ColumnChart`, respectively. The functions allow creating charts using theme and style
attributes.


## Resolved issues
- `TextComponent` with custom rotation wasn’t measured properly in `HorizontalAxis`. As a result
the rendered text didn’t use all available space and was truncated. #77
- Persistent markers added to a `Chart` contained in a `ComposedChart` were clipped.

## API changes
- `ChartModel` has been renamed to `ChartValues` as it better represents its purpose.
- `LineChart`’s constructors now accept single `LineChart.LineSpec`, or a `List` of 
`LineChart.LineSpec`. The `LineChart.LineSpec` defines a style of a data set line.

## Dependency updates
- All modules use Kotlin 1.6.21.
- `compose`, `compose-m2`, and `compose-m3` use Jetpack Compose version `1.2.0-beta02`.
- `compose-m3` uses `compose.material3` version `1.0.0-alpha12`.