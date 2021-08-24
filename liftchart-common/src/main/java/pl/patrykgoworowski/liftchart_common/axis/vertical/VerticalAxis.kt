package pl.patrykgoworowski.liftchart_common.axis.vertical

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.*
import pl.patrykgoworowski.liftchart_common.axis.AxisPosition
import pl.patrykgoworowski.liftchart_common.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.axis.vertical.VerticalAxis.HorizontalLabelPosition.Inside
import pl.patrykgoworowski.liftchart_common.axis.vertical.VerticalAxis.HorizontalLabelPosition.Outside
import pl.patrykgoworowski.liftchart_common.axis.vertical.VerticalAxis.VerticalLabelPosition.Center
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.HorizontalPosition
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.component.text.VerticalPosition
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

    private val isLeft = position.isLeft(isLTR)

    private val isLabelOutsideOnLeftOrInsideOnRight: Boolean
        get() = (horizontalLabelPosition == Outside && isLeft) ||
                (horizontalLabelPosition == Inside && !isLeft)

    private val textHorizontalPosition: HorizontalPosition
        get() = if (isLabelOutsideOnLeftOrInsideOnRight) HorizontalPosition.End else HorizontalPosition.Start

    var maxLabelCount: Int = DEF_LABEL_COUNT
    var labelSpacing: Float = DEF_LABEL_SPACING

    var horizontalLabelPosition = Outside
    var verticalLabelPosition = Center

    override fun drawBehindDataSet(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val axisStep = bounds.height() / drawLabelCount

        var centerY: Float

        for (index in 0..drawLabelCount) {
            centerY = bounds.bottom - (axisStep * index) + (axisThickness / 2)

            guideline?.setParentBounds(bounds)
            guideline?.takeIf {
                it.fitsInHorizontal(
                    dataSetBounds.left,
                    dataSetBounds.right,
                    centerY,
                    dataSetBounds
                )
            }?.drawHorizontal(
                canvas = canvas,
                left = dataSetBounds.left,
                right = dataSetBounds.right,
                centerY = centerY
            )
        }
    }

    override fun drawAboveDataSet(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState
    ) {
        val drawLabelCount = getDrawLabelCount(bounds.height().toInt())

        val labels = getLabels(model, dataSetModel, drawLabelCount)
        val labelHeight = label?.getHeight(includeMargin = false) ?: 0f
        val labelTextHeight = label?.getHeight(includePadding = false, includeMargin = false) ?: 0f
        val axisStep = bounds.height() / drawLabelCount

        val tickLeftX = if (isLabelOutsideOnLeftOrInsideOnRight) {
            bounds.right - (axisThickness + tickLength)
        } else {
            bounds.left
        }

        val tickRightX = if (isLabelOutsideOnLeftOrInsideOnRight) {
            bounds.right
        } else {
            bounds.left + axisThickness + tickLength
        }

        val labelX = if (isLabelOutsideOnLeftOrInsideOnRight) tickLeftX else tickRightX

        var tickCenterY: Float
        val textPosition = verticalLabelPosition.textPosition

        for (index in 0..drawLabelCount) {
            tickCenterY = bounds.bottom - (axisStep * index) + (axisThickness / 2)

            tick?.run {
                setParentBounds(bounds)
                drawHorizontal(
                    canvas = canvas,
                    left = tickLeftX,
                    right = tickRightX,
                    tickCenterY
                )
            }

            label?.let { label ->
                val labelTop = label.getTextTopPosition(textPosition, tickCenterY, labelTextHeight)
                labels.getOrNull(index)?.let { labelText ->
                    if (
                        (horizontalLabelPosition == Inside) &&
                        (isNotInRestrictedBounds(
                            labelX,
                            labelTop - labelHeight.half,
                            labelX,
                            labelTop + labelHeight.half
                        )) || horizontalLabelPosition == Outside
                    ) {
                        label.background?.setParentBounds(bounds)
                        label.drawText(
                            canvas,
                            labelText,
                            labelX,
                            tickCenterY,
                            horizontalPosition = textHorizontalPosition,
                            verticalPosition = verticalLabelPosition.textPosition,
                        )
                    }
                }
            }
        }
        axis?.setParentBounds(bounds)
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
     * in [drawBehindDataSet] and [drawAboveDataSet] functions.
     */
    override fun getDesiredWidth(
        labels: List<String>,
    ): Float {
        val maxLabelAndTickWidth = when (horizontalLabelPosition) {
            Outside -> label?.let { label ->
                labels.maxOf(label::getWidth)
            }.orZero + tickLength
            Inside -> 0f
        }
        return axisThickness.half + maxLabelAndTickWidth
    }

    enum class HorizontalLabelPosition {
        Outside, Inside
    }

    enum class VerticalLabelPosition(val textPosition: VerticalPosition) {
        Center(VerticalPosition.Center),
        Top(VerticalPosition.Bottom),
        Bottom(VerticalPosition.Top),
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