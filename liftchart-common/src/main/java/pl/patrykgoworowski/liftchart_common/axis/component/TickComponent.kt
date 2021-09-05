package pl.patrykgoworowski.liftchart_common.axis.component

import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape

open class TickComponent(
    color: Int,
    thickness: Float,
    var length: Float = 2 * thickness,
    shape: Shape = RectShape,
    dynamicShader: DynamicShader? = null
) : LineComponent(color, thickness, shape, dynamicShader)