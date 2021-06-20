package pl.patrykgoworowski.liftchart_common.axis.horizontal

import android.graphics.Canvas
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
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.component.text.VerticalPosition
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
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

    private val HorizontalAxisPosition.textVerticalPosition: VerticalPosition
        get() = if (isBottom) VerticalPosition.Top else VerticalPosition.Bottom

    public var tickType: TickType = TickType.Minor

    override fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        segmentProperties: SegmentProperties,
        position: HorizontalAxisPosition,
    ) {
        val tickMarkTop = if (position.isBottom) {
            bounds.top
        } else {
            bounds.bottom - tickLength
        }
        val tickMarkBottom = tickMarkTop + axisThickness + tickLength

        val entriesLength = (bounds.width() / segmentProperties.segmentWidth).toInt()
        val tickCount: Int
        val tickDrawStep = segmentProperties.segmentWidth
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
                    valueFormatter.formatValue(valueIndex, index, model),
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
            centerY = if (position is BottomAxis) {
                bounds.top + axis?.thickness?.half.orZero
            } else {
                bounds.bottom + axis?.thickness?.half.orZero
            }
        )
        label?.clearLayoutCache()
    }

    override fun getDrawExtends(
        outDimensions: MutableDimensions,
        model: EntriesModel
    ): Dimensions {
        outDimensions.setVertical(0f)
        return outDimensions.setHorizontal(
            if (tickType == TickType.Minor) tick?.thickness?.half.orZero
            else 0f
        )
    }

    override fun getHeight(
        position: HorizontalAxisPosition,
    ): Int {
        return ((if (position.isBottom) axisThickness else 0f)
                + tickLength
                + label?.getHeight().orZero
                ).toInt()
    }

    enum class TickType {
        Minor, Major
    }

}