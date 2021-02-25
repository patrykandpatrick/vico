package pl.patrykgoworowski.liftchart_common.axis.component

import pl.patrykgoworowski.liftchart_common.axis.AlwaysDrawRule
import pl.patrykgoworowski.liftchart_common.axis.AxisComponentDrawRule
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape

public open class GuidelineComponent(
    color: Int,
    thickness: Float,
    shape: Shape = RectShape(),
    public var drawRule: AxisComponentDrawRule = AlwaysDrawRule,
    ) : RectComponent(color, thickness, shape) {
}