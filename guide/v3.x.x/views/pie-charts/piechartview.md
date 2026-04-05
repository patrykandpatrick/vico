# PieChartView

[`PieChartView`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie/-pie-chart-view/) is the entry point for pie charts in the view system. It accepts [`PieChart`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie/-pie-chart/) and [`PieChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model-producer/) instances and displays the chart.

Use [`PieChartView.modelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie/-pie-chart-view/model-producer) to connect a [`PieChartModelProducer`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model-producer/) instance, or use [`PieChartView.model`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie/-pie-chart-view/model) for a manually created [`PieChartModel`](https://api.vico.patrykandpatrick.com/vico/views/com.patrykandpatrick.vico.views.pie.data/-pie-chart-model/) instance.

## XML styling

[`attrs.xml`](https://github.com/patrykandpatrick/vico/blob/stable/vico/views/src/main/res/values/attrs.xml) defines dedicated style groups for pie charts:

* `PieChartStyle`
* `PieChartSliceStyle`
* `PieChartSliceLabelStyle`

These cover the chart’s inner and outer sizes, slice spacing, the start angle, slice styling, and inside or outside labels.

## Sample charts

* [“Basic pie chart”](https://github.com/patrykandpatrick/vico/blob/stable/sample/charts/views/src/main/kotlin/com/patrykandpatrick/vico/sample/charts/views/BasicPieChart.kt)
