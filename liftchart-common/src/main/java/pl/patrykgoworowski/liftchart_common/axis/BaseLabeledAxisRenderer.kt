package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.set

public abstract class BaseLabeledAxisRenderer<Position: AxisPosition>(
    override var label: TextComponent,
    override var axis: RectComponent,
    override var tick: TickComponent,
    override var guideline: GuidelineComponent,
    public var textPadding: Float,
) : AxisRenderer<Position> {

    override val bounds: RectF = RectF()
    override val dataSetBounds: RectF = RectF()

    override val axisThickness: Float
        get() = axis.thickness

    override var isVisible: Boolean = true

    override var valueFormatter: AxisValueFormatter = DefaultAxisFormatter

    override fun setBounds(left: Number, top: Number, right: Number, bottom: Number) {
        bounds.set(left, top, right, bottom)
    }

    override fun setDataSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        dataSetBounds.set(left, top, right, bottom)
    }

}