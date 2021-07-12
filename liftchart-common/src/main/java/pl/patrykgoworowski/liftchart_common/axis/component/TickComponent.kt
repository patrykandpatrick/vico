package pl.patrykgoworowski.liftchart_common.axis.component

import pl.patrykgoworowski.liftchart_common.component.LineComponent
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_common.path.rectShape


public open class TickComponent(
    color: Int,
    thickness: Float,
    public var length: Float = 2 * thickness,
    shape: Shape = rectShape(),
) : LineComponent(color, thickness, shape) {
}