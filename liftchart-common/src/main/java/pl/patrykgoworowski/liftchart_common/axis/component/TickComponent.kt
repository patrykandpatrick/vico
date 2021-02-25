package pl.patrykgoworowski.liftchart_common.axis.component

import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape


public open class TickComponent(
    color: Int,
    thickness: Float,
    public var length: Float = 2 * thickness,
    shape: Shape = RectShape(),
) : RectComponent(color, thickness, shape) {
}