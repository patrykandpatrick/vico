package pl.patrykgoworowski.liftchart_common.axis.horizontal

import android.graphics.Canvas
import android.graphics.Paint
import pl.patrykgoworowski.liftchart_common.DEF_AXIS_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_GUIDELINE_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_TICK_COMPONENT
import pl.patrykgoworowski.liftchart_common.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.BottomAxis
import pl.patrykgoworowski.liftchart_common.axis.HorizontalAxisPosition
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import kotlin.properties.Delegates.observable

class HorizontalAxis(
    label: TextComponent = DEF_LABEL_COMPONENT,
    axis: RectComponent = DEF_AXIS_COMPONENT,
    tick: TickComponent = DEF_TICK_COMPONENT,
    guideline: GuidelineComponent = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<HorizontalAxisPosition>(label, axis, tick, guideline) {

    public var tickType: TickType = TickType.Minor

    override var isLTR: Boolean by observable(true) { _, _, value ->
        label.isLTR = isLTR
    }

    override var isVisible: Boolean = true

    init {
        label.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas, model: AxisModel, position: HorizontalAxisPosition) {
        val tickMarkTop = if (position.isBottom) {
            bounds.top
        } else {
            bounds.bottom - tick.length
        }
        val tickMarkBottom = tickMarkTop + axis.thickness + tick.length
        val halfLabelSize = label.getHeight().half
        val textY = if (position.isBottom) {
            tickMarkBottom + halfLabelSize
        } else {
            tickMarkTop - halfLabelSize
        }

        val entriesLength = model.getEntriesLength()
        val tickCount: Int
        val tickDrawStep = bounds.width() / entriesLength
        var tickDrawCenter: Float
        var textDrawCenter = bounds.left + tickDrawStep.half

        when (tickType) {
            TickType.Minor -> {
                tickCount = entriesLength + 1
                tickDrawCenter = bounds.left
            }
            TickType.Major -> {
                tickCount = entriesLength
                tickDrawCenter = textDrawCenter
            }
        }

        var valueIndex: Float = model.minX

        val guidelineTop = dataSetBounds.top
        val guidelineBottom = dataSetBounds.bottom

        for (index in 0 until tickCount) {

            tick.drawVertical(
                canvas = canvas,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = tickDrawCenter
            )

            if (guideline.shouldDraw &&
                guideline.fitsInVertical(
                    guidelineTop,
                    guidelineBottom,
                    tickDrawCenter,
                    dataSetBounds
                )
            ) {
                guideline.drawVertical(
                    canvas = canvas,
                    top = guidelineTop,
                    bottom = guidelineBottom,
                    centerX = tickDrawCenter
                )
            }

            if (index < entriesLength) {
                label.drawTextCenteredVertically(
                    canvas,
                    valueFormatter.formatValue(valueIndex, model),
                    textDrawCenter,
                    textY
                )
                valueIndex += model.step
            }
            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        axis.drawHorizontal(
            canvas = canvas,
            left = dataSetBounds.left,
            right = dataSetBounds.right,
            centerY = if (position is BottomAxis) {
                bounds.top + axis.thickness.half
            } else {
                bounds.bottom + axis.thickness.half
            }
        )
    }

    override fun getDrawExtends(
        outDimensions: Dimensions<Float>,
        model: EntriesModel
    ): Dimensions<Float> {
        outDimensions.setVertical(0f)
        return outDimensions.setHorizontal(if (tickType == TickType.Minor) tick.thickness.half else 0f)
    }

    override fun getSize(model: EntriesModel, position: HorizontalAxisPosition): Float {
        return (if (position.isBottom) axis.thickness else 0f) + tick.length + label.getHeight()
    }

    enum class TickType {
        Minor, Major
    }

}