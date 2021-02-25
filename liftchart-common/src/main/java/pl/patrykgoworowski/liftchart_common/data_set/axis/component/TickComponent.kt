package pl.patrykgoworowski.liftchart_common.data_set.axis.component

import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape


public open class TickComponent(
    color: Int,
    thickness: Float,
    public var length: Float = 2 * thickness,
    shape: Shape = RectShape(),
) : AxisComponent(color, thickness, shape) {
}