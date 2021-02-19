package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import android.graphics.Color
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
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
                bounds.left,
                bounds.bottom - (line.thickness + padding),
                bounds.right,
                bounds.bottom - padding
            )
            BottomAxis -> axisBounds.set(
                bounds.left,
                bounds.top + padding,
                bounds.right,
                bounds.top + line.thickness + padding
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        line.draw(canvas, axisBounds)

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
        var lastSegmentPos = bounds.left

        while (lastSegmentPos + tick.thickness < bounds.right) {
            tickMarkCenter = lastSegmentPos + (model.xSegmentWidth / 2)
            tick.drawVertical(
                canvas = canvas,
                top = tickMarkTop,
                bottom = tickMarkBottom,
                centerX = tickMarkCenter
            )

            if (guideline.shouldDraw) {
                guideline.drawVertical(
                    canvas = canvas,
                    top = dataSetBounds.top - if (isBottom) 0f else padding,
                    bottom = dataSetBounds.bottom + if (isBottom) padding else 0f,
                    centerX = tickMarkCenter
                )
            }

            model.entries.getOrNull(index)?.x?.let { value ->
                canvas.drawText(
                    valueFormatter.formatValue(value, model),
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
        return line.thickness + padding + tickMarkLength + textPadding + labelPaint.textHeight
    }

}