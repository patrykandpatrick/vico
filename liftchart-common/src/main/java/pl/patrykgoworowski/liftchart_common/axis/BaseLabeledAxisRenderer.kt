package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.orZero
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_common.extension.setAll
import kotlin.properties.Delegates

public abstract class BaseLabeledAxisRenderer<Position : AxisPosition>(
    override var label: TextComponent?,
    override var axis: LineComponent?,
    override var tick: TickComponent?,
    override var guideline: LineComponent?,
) : AxisRenderer<Position> {

    protected val labels = ArrayList<String>()

    override val bounds: RectF = RectF()
    override val restrictedBounds: MutableList<RectF> = mutableListOf()
    override val dataSetBounds: RectF = RectF()

    override val axisThickness: Float
        get() = axis?.thickness.orZero

    override val tickLength: Float
        get() = tick?.length.orZero

    override val tickThickness: Float
        get() = tick?.thickness.orZero

    override val guidelineThickness: Float
        get() = guideline?.thickness.orZero

    override var isLTR: Boolean by Delegates.observable(true) { _, _, value ->
        label?.isLTR = value
    }

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        bounds.set(left, top, right, bottom)
    }

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
    }

    override fun setRestrictedBounds(vararg bounds: RectF?) {
        restrictedBounds.setAll(bounds.filterNotNull())
    }

    protected fun isNotInRestrictedBounds(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Boolean = restrictedBounds.none {
        it.contains(left, top, right, bottom) || it.intersects(left, top, right, bottom)
    }

}