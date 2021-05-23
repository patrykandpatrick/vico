package pl.patrykgoworowski.liftchart_common.axis.component

import pl.patrykgoworowski.liftchart_common.axis.AlwaysDrawRule
import pl.patrykgoworowski.liftchart_common.axis.AxisComponentDrawRule
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_common.path.rectShape

public open class GuidelineComponent(
    color: Int,
    thickness: Float,
    shape: Shape = rectShape(),
    public var drawRule: AxisComponentDrawRule = AlwaysDrawRule,
    ) : RectComponent(color, thickness, shape) {
}