# Vico Chart

## Getting started

## Modules
| Group | Description |
| --- | --- |
| `vico.compose` | TBD |
| `vico.compose-m2` | TBD |
| `vico.compose-m3` | TBD |
| `vico.core` | TBD |
| `vico.view` | TBD |

## Jetpack Compose
A basic column chart with two axes and five entries can be added as follows:
```kt
Chart(
    chart = columnChart(),
    model = entryModelOf(5f, 15f, 10f, 20f, 10f),
    startAxis = startAxis(),
    bottomAxis = bottomAxis(),
)
```

## Views
A basic column chart with two axes and five entries can be added as follows. First, add a
`ChartView` to your XML layout file:
```xml
<pl.patrykgoworowski.vico.view.chart.ChartView
    android:id="@+id/chart"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:chartType="column"
    app:showBottomAxis="true"
    app:showStartAxis="true" />
```
Then, set the model for the chart:
```kt
findViewById<ChartView>(R.id.chart).model = entryModelOf(5f, 15f, 10f, 20f, 10f)
```
