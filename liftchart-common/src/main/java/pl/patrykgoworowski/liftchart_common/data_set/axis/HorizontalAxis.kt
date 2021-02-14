package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import android.graphics.Color
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.extension.textHeight

class HorizontalAxis(
    override val position: HorizontalAxisPosition,
    textSize: Float = 32f,
    textColor: Int = Color.DKGRAY,
) : BaseLabeledAxisRenderer(position, textSize, textColor) {

    private val isBottom = position == BottomAxis

    override var isLTR: Boolean = false
    override var isVisible: Boolean = true

    override fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        when (position) {
            TopAxis -> axisBounds.set(
                drawBounds.left,
                drawBounds.bottom - (axisThickness + padding),
                drawBounds.right,
                drawBounds.bottom - padding
            )
            BottomAxis -> axisBounds.set(
                drawBounds.left,
                drawBounds.top + padding,
                drawBounds.right,
                drawBounds.top + axisThickness + padding
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        canvas.drawRect(axisBounds, axisPaint)

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

        var index = 0
        var tickMarkCenter: Float
        var lastSegmentPos = drawBounds.left

        while (lastSegmentPos + tickMarkThickness < drawBounds.right) {
            tickMarkCenter = lastSegmentPos + (model.xSegmentWidth / 2)
            canvas.drawRect(
                tickMarkCenter - (tickMarkThickness / 2),
                tickMarkTop,
                tickMarkCenter + (tickMarkThickness / 2),
                tickMarkBottom,
                axisPaint
            )
            model.entries.getOrNull(index)?.x?.toString()?.let { labelText ->
                canvas.drawText(
                    labelText,
                    tickMarkCenter,
                    textY,
                    labelPaint
                )
            }

            lastSegmentPos += (model.xSegmentWidth / model.step) + model.xSegmentSpacing
            index++
        }
    }

    override fun getSize(model: EntriesModel): Float {
        return axisThickness + padding + tickMarkLength + textPadding + labelPaint.textHeight
    }

}