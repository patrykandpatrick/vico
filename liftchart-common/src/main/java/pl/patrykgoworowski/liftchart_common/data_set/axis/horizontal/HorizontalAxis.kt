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
    private val isTop = position == TopAxis

    private val tickDrawBounds = RectF()

    public var tickType: TickType = TickType.Minor

    override var isLTR: Boolean = false
    override var isVisible: Boolean = true

    init {
//        axis.thickness = 16f
    }

    private fun updateAxisBounds() {
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

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        updateAxisBounds()
        updateTickDrawBounds()

        val tickMarkTop = if (isBottom) {
            axisBounds.top
        } else {
            axisBounds.top - tick.length
        }
        val tickMarkBottom = tickMarkTop + axis.thickness + tick.length
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
                canvas.drawText(
                    valueFormatter.formatValue(valueIndex, model),
                    textDrawCenter,
                    textY,
                    labelPaint
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

    override fun getSize(model: EntriesModel): Float {
        return (if (isBottom) axis.thickness else 0f) +
                tick.length + textPadding + labelPaint.textHeight
    }

    enum class TickType {
        Minor, Major
    }

}