package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.*
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.HorizontalPosition
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero

class VerticalAxis<Position : AxisPosition.Vertical> private constructor(
    override val position: Position,
    label: TextComponent?,
    axis: LineComponent?,
    tick: TickComponent?,
    guideline: LineComponent?,
) : BaseLabeledAxisRenderer<Position>(label, axis, tick, guideline) {

    private val AxisPosition.Vertical.textHorizontalPosition: HorizontalPosition
        get() = if (isStart) HorizontalPosition.End else HorizontalPosition.Start

    var maxLabelCount: Int = DEF_LABEL_COUNT
    var labelSpacing: Float = DEF_LABEL_SPACING

    override fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        val isLeft = position.isLeft(isLTR)
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(model, dataSetModel, drawLabelCount)
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
        entryModel: EntriesModel,
        dataSetModel: DataSetModel,
        maxLabelCount: Int = this.maxLabelCount,
    ): List<String> {
        labels.clear()
        val step = (dataSetModel.maxY - dataSetModel.minY) / maxLabelCount
        for (index in maxLabelCount downTo 0) {
            val value = dataSetModel.maxY - (step * index)
            labels += valueFormatter.formatValue(value, index, entryModel, dataSetModel)
        }
        return labels
    }

    override fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): Dimensions = outDimensions

    override fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): Dimensions {
        val labels = getLabels(model, dataSetModel, getDrawLabelCount(availableHeight.toInt()))
        if (labels.isEmpty()) return outDimensions.set(0f)

        fun getHalfLabelHeight(text: String): Float =
            label?.getHeight(text)?.half.orZero

        return outDimensions.set(
            start = if (position.isStart) getDesiredWidth(labels) else 0f,
            top = getHalfLabelHeight(labels.first()) - axisThickness,
            end = if (position.isEnd) getDesiredWidth(labels) else 0f,
            bottom = getHalfLabelHeight(labels.last())
        )
    }

    override fun getDesiredHeight(): Int = 0

    /**
     * Calculates a width of this [VerticalAxis] by calculating:
     * — Widest label width from passed [labels],
     * — [axisThickness],
     * — [tickLength].
     * @return Width of this [VerticalAxis] that should be enough to fit its contents
     * in [draw] function.
     */
    override fun getDesiredWidth(
        labels: List<String>,
    ): Float {
        val widestTextComponentWidth = label?.let { label ->
            labels.maxOf(label::getWidth)
        }.orZero
        return axisThickness.half + tickLength + widestTextComponentWidth
    }

    companion object {

        fun start(
            label: TextComponent? = DEF_LABEL_COMPONENT,
            axis: LineComponent? = DEF_AXIS_COMPONENT,
            tick: TickComponent? = DEF_TICK_COMPONENT,
            guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
        ): VerticalAxis<AxisPosition.Vertical.Start> = VerticalAxis(
            position = AxisPosition.Vertical.Start,
            label = label,
            axis = axis,
            tick = tick,
            guideline = guideline,
        )

        fun end(
            label: TextComponent? = DEF_LABEL_COMPONENT,
            axis: LineComponent? = DEF_AXIS_COMPONENT,
            tick: TickComponent? = DEF_TICK_COMPONENT,
            guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
        ): VerticalAxis<AxisPosition.Vertical.End> = VerticalAxis(
            position = AxisPosition.Vertical.End,
            label = label,
            axis = axis,
            tick = tick,
            guideline = guideline,
        )
    }

}