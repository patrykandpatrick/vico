package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.extension.half
import kotlin.properties.Delegates.observable

class VerticalAxis(
    override val position: VerticalAxisPosition,
    textSize: Float = 32f,
    textColor: Int = Color.DKGRAY,
) : BaseLabeledAxisRenderer(position, textSize, textColor) {

    companion object {
        const val TEXT_MEASUREMENT_CHAR = "1"
    }

    private val isLeft: Boolean
        get() = (position == StartAxis && isLTR) || (position == EndAxis && !isLTR)

    private val labels = ArrayList<String>()
    private val xTextBounds = Rect()

    override var isLTR: Boolean by observable(true) { _, _, isLTR ->
        labelPaint.textAlign = if (isLeft) {
            Paint.Align.RIGHT
        } else {
            Paint.Align.LEFT
        }
    }
    override var isVisible: Boolean = true

    var tickCount = 4

    private fun updateAxisBounds() {
        val topBound = bounds.top
        val bottomBound = bounds.bottom + axis.thickness
        when (position) {
            StartAxis -> axisBounds.set(
                bounds.right - axis.thickness,
                topBound,
                bounds.right,
                bottomBound
            )
            EndAxis -> axisBounds.set(
                bounds.left,
                topBound,
                bounds.left + axis.thickness,
                bottomBound
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        updateAxisBounds()

        val labels = getLabels(model)
        val axisStep = bounds.height() / tickCount

        val tickLeftX = if (isLeft) {
            axisBounds.left - tick.length
        } else {
            axisBounds.right
        }

        val tickRightX = if (isLeft) {
            axisBounds.left
        } else {
            axisBounds.right + tick.length
        }

        val labelX = if (isLeft) {
            tickLeftX - textPadding
        } else {
            tickRightX + textPadding
        }

        var tickCenterY: Float
        var textY: Float

        for (index in 0..tickCount) {

            tickCenterY = bounds.bottom - (axisStep * index) + (axis.thickness / 2)

            tick.drawHorizontal(
                canvas = canvas,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            if (guideline.shouldDraw && guideline.drawRule.getShouldDraw(index, tickCount + 1)) {
                guideline.drawHorizontal(
                    canvas = canvas,
                    left = dataSetBounds.left,
                    right = dataSetBounds.right,
                    centerY = tickCenterY
                )
            }

            labelPaint.getTextBounds(
                TEXT_MEASUREMENT_CHAR,
                0,
                TEXT_MEASUREMENT_CHAR.length,
                xTextBounds
            )
            textY = tickCenterY + (xTextBounds.height() / 2)
            labels.getOrNull(index)?.let { label ->
                canvas.drawText(
                    label,
                    labelX,
                    textY,
                    labelPaint
                )
            }
        }
        axis.draw(canvas, axisBounds)
    }

    private fun getLabels(model: EntriesModel): List<String> {
        labels.clear()
        val step = model.maxY / tickCount
        for (index in tickCount downTo 0) {
            val value = (model.maxY - (step * index))
            labels += valueFormatter.formatValue(value, model)
        }
        return labels
    }

    override fun getSize(model: EntriesModel): Float {
        val widestTextWidth = getLabels(model).maxOf { label ->
            labelPaint.measureText(label)
        }
        return axis.thickness.half + tick.length + textPadding + widestTextWidth
    }

}