package pl.patrykgoworowski.liftchart_common.data_set.axis.horizontal

import android.graphics.Canvas
import android.graphics.Color
import pl.patrykgoworowski.liftchart_common.data_set.axis.BaseLabeledAxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.BottomAxis
import pl.patrykgoworowski.liftchart_common.data_set.axis.HorizontalAxisPosition
import pl.patrykgoworowski.liftchart_common.data_set.axis.TopAxis
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
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

        var index = 0
        var tickMarkCenter: Float
        var lastSegmentPos = bounds.left
        var lastSegmentCenter: Float


        while (lastSegmentPos + tick.thickness < bounds.right) {

            lastSegmentCenter = lastSegmentPos + (model.xSegmentWidth / 2)

            tickMarkCenter = when(tickType) {
                TickType.Minor -> lastSegmentPos + model.xSegmentWidth + (model.xSegmentSpacing / 2)
                TickType.Major -> lastSegmentCenter
            }

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
                    lastSegmentCenter,
                    textY,
                    labelPaint
                )
            }

            lastSegmentPos += (model.xSegmentWidth / model.step) + model.xSegmentSpacing
            index++
        }
    }

    override fun getSize(model: EntriesModel): Float {
        return axis.thickness + padding + tickMarkLength + textPadding + labelPaint.textHeight
    }

    enum class TickType {
        Minor, Major
    }

}