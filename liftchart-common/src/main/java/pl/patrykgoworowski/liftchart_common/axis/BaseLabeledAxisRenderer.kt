package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Color
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.DefaultAxisFormatter
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.set

public abstract class BaseLabeledAxisRenderer<Position: AxisPosition>(
    override var label: TextComponent = TextComponent(),
    override var axis: RectComponent = RectComponent(Color.BLUE, 4f),
    override var tick: TickComponent = TickComponent(Color.BLUE, 4f),
    override var guideline: GuidelineComponent = GuidelineComponent(Color.GRAY, 4f),
) : AxisRenderer<Position> {

    protected val axisBounds = RectF()

    var textPadding = 12f

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