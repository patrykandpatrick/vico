package pl.patrykgoworowski.liftchart_common.component.dimension

import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

open class DefaultMargins : Margins {

    override val margins: MutableDimensions = MutableDimensions(0f, 0f, 0f, 0f)

}