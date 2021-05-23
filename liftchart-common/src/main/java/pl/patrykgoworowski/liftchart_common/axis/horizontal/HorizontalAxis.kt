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
import pl.patrykgoworowski.liftchart_common.axis.HorizontalAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent.VerticalPosition
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero

class HorizontalAxis(
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: RectComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: GuidelineComponent? = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<HorizontalAxisPosition>(label, axis, tick, guideline),
    HorizontalAxisRenderer {

    public var tickType: TickType = TickType.Minor

    override var isVisible: Boolean = true

    init {
        label?.textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas, model: EntriesModel, position: HorizontalAxisPosition) {
        val tickMarkTop = if (position.isBottom) {
            bounds.top
        } else {
            bounds.bottom - tickLength
        }
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength

        val entriesLength = model.getEntriesLength()
        val tickCount: Int
        val tickDrawStep = bounds.width() / entriesLength
        var tickDrawCenter: Float
        var textDrawCenter = bounds.left + tickDrawStep.half

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

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

            tick?.drawVertical(
                canvas = canvas,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = tickDrawCenter
            )

            guideline?.takeIf {
                it.shouldDraw &&
                        it.fitsInVertical(
                            guidelineTop,
                            guidelineBottom,
                            tickDrawCenter,
                            dataSetBounds
                        )
            }?.drawVertical(
                canvas = canvas,
                top = guidelineTop,
                bottom = guidelineBottom,
                centerX = tickDrawCenter
            )

            if (index < entriesLength) {
                label?.drawTextVertically(
                    canvas,
                    valueFormatter.formatValue(valueIndex, model),
                    textDrawCenter,
                    textY,
                    if (position.isBottom) VerticalPosition.Top else VerticalPosition.Bottom,
                    tickDrawStep.toInt()
                )
                valueIndex += model.step
            }
            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        axis?.drawHorizontal(
            canvas = canvas,
            left = dataSetBounds.left,
            right = dataSetBounds.right,
            centerY = if (position is BottomAxis) {
                bounds.top + axis?.thickness?.half.orZero
            } else {
                bounds.bottom + axis?.thickness?.half.orZero
            }
        )
        label?.clearLayoutCache()
    }

    override fun getDrawExtends(
        outDimensions: MutableDimensions<Float>,
        model: EntriesModel
    ): Dimensions<Float> {
        outDimensions.setVertical(0f)
        return outDimensions.setHorizontal(
            if (tickType == TickType.Minor) tick?.thickness?.half.orZero
            else 0f
        )
    }

    override fun getHeight(
        model: EntriesModel,
        position: HorizontalAxisPosition,
        width: Float
    ): Float {
        val labelWidth = (width / model.getEntriesLength()).toInt()
        val highestLabelHeight = getLabels(model)
            .maxOf { label?.getHeight(it, labelWidth).orZero }
        return (if (position.isBottom) axisThickness else 0f) + tickLength + highestLabelHeight
    }

    private fun getLabels(model: EntriesModel): List<String> {
        labels.clear()
        val range = (model.maxX - model.minX).toInt()
        for (index in range downTo 0) {
            val value = (model.maxX - (model.step * index))
            labels += valueFormatter.formatValue(value, model)
        }
        return labels
    }

    enum class TickType {
        Minor, Major
    }

}