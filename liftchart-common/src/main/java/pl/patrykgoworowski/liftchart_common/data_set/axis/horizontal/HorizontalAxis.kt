package pl.patrykgoworowski.liftchart_common.data_set.axis.horizontal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.BottomAxis
import pl.patrykgoworowski.liftchart_common.data_set.axis.HorizontalAxisPosition
import pl.patrykgoworowski.liftchart_common.data_set.axis.TopAxis
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.textHeight

class HorizontalAxis(
    override val position: HorizontalAxisPosition,
    textSize: Float = 32f,
    textColor: Int = Color.DKGRAY,
) : BaseLabeledAxisRenderer(position, textSize, textColor) {

    private val isBottom = position == BottomAxis

    public var tickType: TickType = TickType.Minor

    override var isLTR: Boolean = false
    override var isVisible: Boolean = true

    override fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        when (position) {
            TopAxis -> axisBounds.set(
                bounds.left,
                bounds.bottom - (axis.thickness + padding),
                bounds.right,
                bounds.bottom - padding
            )
            BottomAxis -> axisBounds.set(
                bounds.left,
                bounds.top + padding,
                bounds.right,
                bounds.top + axis.thickness + padding
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        axis.draw(canvas, axisBounds)

        val tickMarkTop = if (isBottom) {
            axisBounds.bottom
        } else {
            axisBounds.top - tickMarkLength
        }
        val tickMarkBottom = tickMarkTop + tickMarkLength
        val textY = if (isBottom) {
            tickMarkBottom + textPadding - labelPaint.ascent()
        } else {
            tickMarkTop - textPadding - labelPaint.descent()
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

        for (index in 0 until tickCount) {

            axis.drawVerticalTick(
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
                canvas.drawText(
                    valueFormatter.formatValue(index + model.minX, model),
                    textDrawCenter,
                    textY,
                    labelPaint
                )
            }
            tickDrawCenter += tickDrawStep
            textDrawCenter += tickDrawStep
        }

        axis.drawAxis(canvas, axisBounds)
    }

    private fun updateTickDrawBounds() {
        val left: Float
        val right: Float

        when (tickType) {
            TickType.Minor -> {
                left = bounds.left - axis.thickness.half
                right = bounds.right + axis.thickness.half
            }
            TickType.Major -> {
                left = bounds.left
                right = bounds.right
            }
        }
        tickDrawBounds.set(left, bounds.top, right, bounds.bottom)
    }

    override fun getSize(model: EntriesModel): Float {
        return axis.thickness + padding + tickMarkLength + textPadding + labelPaint.textHeight
    }

    enum class TickType {
        Minor, Major
    }

}