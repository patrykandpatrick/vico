package pl.patrykgoworowski.liftchart_common.axis.horizontal

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.DEF_AXIS_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_GUIDELINE_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_TICK_COMPONENT
import pl.patrykgoworowski.liftchart_common.axis.AxisPosition
import pl.patrykgoworowski.liftchart_common.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.component.text.VerticalPosition
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.RendererViewState
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero
import kotlin.math.ceil

class HorizontalAxis<Position : AxisPosition.Horizontal> private constructor(
    override val position: Position,
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: LineComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<Position>(label, axis, tick, guideline) {

    private val AxisPosition.Horizontal.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Top else VerticalPosition.Bottom

    public var tickType: TickType = TickType.Minor

    override fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
        rendererViewState: RendererViewState,
    ) {
        val tickMarkTop = if (position.isBottom) {
            bounds.top
        } else {
            bounds.bottom - tickLength
        }
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength
        val scrollX = rendererViewState.horizontalScroll

        val clipRestoreCount = canvas.save()
        canvas.clipRect(
            bounds.left - if (tickType == TickType.Minor) tickThickness.half else 0f,
            minOf(bounds.top, dataSetBounds.top),
            bounds.right + if (tickType == TickType.Minor) tickThickness.half else 0f,
            maxOf(bounds.bottom, dataSetBounds.bottom)
        )

        val entriesLength = ceil(bounds.width() / segmentProperties.segmentWidth).toInt() + 1
        val tickCount: Int
        val tickDrawStep = segmentProperties.segmentWidth
        var tickDrawCenter: Float
        val scrollAdjustment = (scrollX / tickDrawStep).toInt()
        var textDrawCenter = bounds.left + tickDrawStep.half - scrollX + (tickDrawStep * scrollAdjustment)

        val textY = if (position.isBottom) tickMarkBottom else tickMarkTop

        when (tickType) {
            TickType.Minor -> {
                tickCount = entriesLength + 1
                tickDrawCenter = bounds.left - scrollX + (tickDrawStep * scrollAdjustment)
            }
            TickType.Major -> {
                tickCount = entriesLength
                tickDrawCenter = textDrawCenter
            }
        }

        var valueIndex: Float = model.minX + scrollAdjustment * model.step

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
                label?.drawText(
                    canvas,
                    valueFormatter.formatValue(valueIndex, index, model, dataSetModel),
                    textDrawCenter,
                    textY,
                    verticalPosition = position.textVerticalPosition,
                    width = tickDrawStep.toInt(),
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
            centerY = if (position is AxisPosition.Horizontal.Bottom) {
                bounds.top + axis?.thickness?.half.orZero
            } else {
                bounds.bottom + axis?.thickness?.half.orZero
            }
        )
        label?.clearLayoutCache()

        if (clipRestoreCount >= 0) canvas.restoreToCount(clipRestoreCount)
    }

    override fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): Dimensions {
        return outDimensions.apply {
            setHorizontal(
                if (tickType == TickType.Minor) tick?.thickness?.half.orZero
                else 0f
            )
            top = if (position.isTop) getDesiredHeight().toFloat() else 0f
            bottom = if (position.isBottom) getDesiredHeight().toFloat() else 0f
        }
    }

    override fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntriesModel,
        dataSetModel: DataSetModel
    ): Dimensions = outDimensions

    override fun getDesiredHeight(): Int {
        return ((if (position.isBottom) axisThickness else 0f)
                + tickLength
                + label?.getHeight().orZero
                ).toInt()
    }

    override fun getDesiredWidth(
        labels: List<String>
    ): Float = 0f

    enum class TickType {
        Minor, Major
    }

    companion object {
        fun top(
            label: TextComponent? = DEF_LABEL_COMPONENT,
            axis: LineComponent? = DEF_AXIS_COMPONENT,
            tick: TickComponent? = DEF_TICK_COMPONENT,
            guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
        ): HorizontalAxis<AxisPosition.Horizontal.Top> = HorizontalAxis(
            position = AxisPosition.Horizontal.Top,
            label = label,
            axis = axis,
            tick = tick,
            guideline = guideline,
        )

        fun bottom(
            label: TextComponent? = DEF_LABEL_COMPONENT,
            axis: LineComponent? = DEF_AXIS_COMPONENT,
            tick: TickComponent? = DEF_TICK_COMPONENT,
            guideline: LineComponent? = DEF_GUIDELINE_COMPONENT,
        ): HorizontalAxis<AxisPosition.Horizontal.Bottom> = HorizontalAxis(
            position = AxisPosition.Horizontal.Bottom,
            label = label,
            axis = axis,
            tick = tick,
            guideline = guideline,
        )
    }

}