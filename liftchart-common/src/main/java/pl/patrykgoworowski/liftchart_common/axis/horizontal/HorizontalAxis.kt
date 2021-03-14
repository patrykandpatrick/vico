package pl.patrykgoworowski.liftchart_common.axis.horizontal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.axis.BottomAxis
import pl.patrykgoworowski.liftchart_common.axis.HorizontalAxisPosition
import pl.patrykgoworowski.liftchart_common.axis.TopAxis
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.extension.half

class HorizontalAxis(
    label: TextComponent = TextComponent(),
    axis: RectComponent = RectComponent(Color.BLUE, 4f),
    tick: TickComponent = TickComponent(Color.BLUE, 4f),
    guideline: GuidelineComponent = GuidelineComponent(Color.GRAY, 4f),
) : BaseLabeledAxisRenderer<HorizontalAxisPosition>(label, axis, tick, guideline) {

    private val tickDrawBounds = RectF()

    public var tickType: TickType = TickType.Minor

    override var isLTR: Boolean = false
    override var isVisible: Boolean = true

    init {
//        axis.thickness = 16f
        label.textAlign = Paint.Align.CENTER
    }

    private fun updateAxisBounds(position: HorizontalAxisPosition) {
        when (position) {
            TopAxis -> axisBounds.set(
                bounds.left,
                bounds.bottom,
                bounds.right,
                bounds.bottom + axis.thickness
            )
            BottomAxis -> axisBounds.set(
                bounds.left,
                bounds.top,
                bounds.right,
                bounds.top + axis.thickness
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel, position: HorizontalAxisPosition) {
        updateAxisBounds(position)
        updateTickDrawBounds()

        val tickMarkTop = if (position.isBottom) {
            axisBounds.top
        } else {
            axisBounds.top - tick.length
        }
        val tickMarkBottom = tickMarkTop + axis.thickness + tick.length
        val halfLabelSize = label.getHeight().half
        val textY = if (position.isBottom) {
            tickMarkBottom + textPadding + halfLabelSize
        } else {
            tickMarkTop - textPadding - halfLabelSize
        }

        val entriesLength = model.getEntriesLength()
        val tickCount: Int
        val tickDrawStep = tickDrawBounds.width() / entriesLength
        var tickDrawCenter: Float
        var textDrawCenter = tickDrawBounds.left + tickDrawStep.half

        when (tickType) {
            TickType.Minor -> {
                tickCount = entriesLength + 1
                tickDrawCenter = tickDrawBounds.left
            }
            TickType.Major -> {
                tickCount = entriesLength
                tickDrawCenter = textDrawCenter
            }
        }

        var valueIndex: Float = model.minX

        for (index in 0 until tickCount) {

            tick.drawVertical(
                canvas = canvas,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = tickDrawCenter
            )

            if (guideline.shouldDraw) {
                guideline.drawVertical(
                    canvas = canvas,
                    top = dataSetBounds.top,
                    bottom = dataSetBounds.bottom,
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

        axis.draw(canvas, axisBounds)
    }

    private fun updateTickDrawBounds() {
        tickDrawBounds.set(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    override fun getDrawExtends(
        outDimensions: Dimensions<Float>,
        model: EntriesModel
    ): Dimensions<Float> {
        outDimensions.setVertical(0f)
        return outDimensions.setHorizontal(if (tickType == TickType.Minor) tick.thickness.half else 0f)
    }

    override fun getSize(model: EntriesModel, position: HorizontalAxisPosition): Float {
        return (if (position.isBottom) axis.thickness else 0f) +
                tick.length + textPadding + label.getHeight()
    }

    enum class TickType {
        Minor, Major
    }

}