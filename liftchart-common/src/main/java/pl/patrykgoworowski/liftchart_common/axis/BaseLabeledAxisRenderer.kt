package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.orZero
import pl.patrykgoworowski.liftchart_common.extension.set
import kotlin.properties.Delegates

public abstract class BaseLabeledAxisRenderer<Position: AxisPosition>(
    override var label: TextComponent?,
    override var axis: RectComponent?,
    override var tick: TickComponent?,
    override var guideline: GuidelineComponent?,
) : AxisRenderer<Position> {

    protected val labels = ArrayList<String>()

    override val bounds: RectF = RectF()
    override val dataSetBounds: RectF = RectF()

    override val axisThickness: Float
        get() = axis?.thickness.orZero

    override val tickLength: Float
        get() = tick?.length.orZero

    override val tickThickness: Float
        get() = tick?.thickness.orZero

    override var isLTR: Boolean by Delegates.observable(true) { _, _, value ->
        label?.isLTR = value
    }

    override var isVisible: Boolean = true

    override var valueFormatter: AxisValueFormatter = DecimalFormatAxisValueFormatter()

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        bounds.set(left, top, right, bottom)
    }

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
    }

}