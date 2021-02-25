package pl.patrykgoworowski.liftchart_common.data_set.axis.component

import pl.patrykgoworowski.liftchart_common.data_set.axis.AlwaysDrawRule
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisComponentDrawRule
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class GuidelineComponent(
    color: Int,
    thickness: Float,
    shape: Shape = RectShape(),
    public var drawRule: AxisComponentDrawRule = AlwaysDrawRule,
    ) : AxisComponent(color, thickness, shape) {
}