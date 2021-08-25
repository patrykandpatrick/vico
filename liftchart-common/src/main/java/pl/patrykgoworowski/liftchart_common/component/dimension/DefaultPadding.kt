package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

open class DefaultPadding : Padding {

    override val padding: MutableDimensions = MutableDimensions(0f, 0f, 0f, 0f)

}