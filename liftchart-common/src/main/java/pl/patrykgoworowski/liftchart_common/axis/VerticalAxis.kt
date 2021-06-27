package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.*
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.text.HorizontalPosition
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero

class VerticalAxis(
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: RectComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: GuidelineComponent? = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<AxisPosition.Vertical>(label, axis, tick, guideline) {

    private val AxisPosition.Vertical.textHorizontalPosition: HorizontalPosition
        get() = if (isStart) HorizontalPosition.End else HorizontalPosition.Start

    var maxLabelCount: Int = DEF_LABEL_COUNT
    var labelSpacing: Float = DEF_LABEL_SPACING

    override fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        segmentProperties: SegmentProperties,
        position: AxisPosition.Vertical,
    ) {
        val isLeft = position.isLeft(isLTR)
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(model, drawLabelCount)
        val axisStep = bounds.height() / drawLabelCount

        val tickLeftX = if (isLeft) {
            bounds.right - (axisThickness + tickLength)
        } else {
            bounds.left
        }

        val tickRightX = if (isLeft) {
            bounds.right
        } else {
            bounds.left + axisThickness + tickLength
        }

        val labelX = if (isLeft) {
            tickLeftX
        } else {
            tickRightX
        }

        var tickCenterY: Float

        for (index in 0..drawLabelCount) {

            tickCenterY = bounds.bottom - (axisStep * index) + (axisThickness / 2)

            tick?.drawHorizontal(
                canvas = canvas,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            guideline?.takeIf {
                it.fitsInHorizontal(
                    dataSetBounds.left,
                    dataSetBounds.right,
                    tickCenterY,
                    dataSetBounds
                )
            }?.drawHorizontal(
                canvas = canvas,
                left = dataSetBounds.left,
                right = dataSetBounds.right,
                centerY = tickCenterY
            )

            labels.getOrNull(index)?.let { label ->
                this.label?.drawText(
                    canvas,
                    label,
                    labelX,
                    tickCenterY,
                    horizontalPosition = position.textHorizontalPosition,
                )
            }
        }
        axis?.drawVertical(
            canvas = canvas,
            top = bounds.top,
            bottom = bounds.bottom + axisThickness,
            centerX = if (isLeft) {
                bounds.right - axisThickness.half
            } else {
                bounds.left + axisThickness.half
            }
        )
        label?.clearLayoutCache()
    }

    private fun getDrawLabelCount(availableHeight: Int): Int {
        val labelComponent = label ?: return maxLabelCount
        val height = labelComponent.getHeight()
        var result = 0f
        var addition: Float
        for (i in 0 until maxLabelCount) {
            addition = if (i > 0) height + labelSpacing else height
            if (result + addition > availableHeight) return i
            result += addition
        }
        return maxLabelCount
    }

    private fun getLabels(
        model: EntriesModel,
        maxLabelCount: Int = this.maxLabelCount,
    ): List<String> {
        labels.clear()
        val step = model.maxY / maxLabelCount
        for (index in maxLabelCount downTo 0) {
            val value = (model.maxY - (step * index))
            labels += valueFormatter.formatValue(value, index, model)
        }
        return labels
    }

    override fun getInsets(
        outDimensions: MutableDimensions,
        model: EntriesModel
    ): Dimensions {
        val labels = getLabels(model)
        if (labels.isEmpty()) return outDimensions.set(0f)

        fun getHalfLabelHeight(text: String): Float =
            label?.getHeight(text)?.half.orZero

        return outDimensions.set(
            start = 0f,
            top = getHalfLabelHeight(labels.first()) - axisThickness,
            end = 0f,
            bottom = getHalfLabelHeight(labels.last())
        )
    }

    override fun getDesiredHeight(position: AxisPosition.Vertical): Int = 0

    override fun getDesiredWidth(
        model: EntriesModel,
        position: AxisPosition.Vertical,
        availableHeight: Int,
    ): Int {
        val widestTextComponentWidth = label?.let { label ->
            getLabels(model, getDrawLabelCount(availableHeight)).maxOf { labelText ->
                label.getWidth(labelText)
            }
        }.orZero
        return (axisThickness + tickLength + widestTextComponentWidth).toInt()
    }

}