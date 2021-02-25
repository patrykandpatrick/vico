package pl.patrykgoworowski.liftchart_common.axis

import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis

class AxisManager(
    var startAxis: AxisRenderer = VerticalAxis(StartAxis),
    var topAxis: AxisRenderer = HorizontalAxis(TopAxis),
) {
}