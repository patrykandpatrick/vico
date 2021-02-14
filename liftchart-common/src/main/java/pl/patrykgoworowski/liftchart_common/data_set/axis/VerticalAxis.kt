package pl.patrykgoworowski.liftchart_common.data_set.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
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

    override fun onSetBounds(left: Number, top: Number, right: Number, bottom: Number) {
        val topBound = drawBounds.top - (tickMarkThickness / 2)
        val bottomBound = drawBounds.bottom + (tickMarkThickness / 2)
        when (position) {
            StartAxis -> axisBounds.set(
                drawBounds.right - (axisThickness + padding),
                topBound,
                drawBounds.right - padding,
                bottomBound
            )
            EndAxis -> axisBounds.set(
                drawBounds.left + padding,
                topBound,
                drawBounds.left + axisThickness + padding,
                bottomBound
            )
        }
    }

    override fun onDraw(canvas: Canvas, model: AxisModel) {
        canvas.drawRect(axisBounds, axisPaint)

        val labels = getLabels(0f, model.maxY)
        val axisStep = drawBounds.height() / tickCount

        val tickLeftX = if (isLeft) {
            axisBounds.left - tickMarkLength
        } else {
            axisBounds.right
        }

        val tickRightX = if (isLeft) {
            axisBounds.left
        } else {
            axisBounds.right + tickMarkLength
        }

        val labelX = if (isLeft) {
            tickLeftX - textPadding
        } else {
            tickRightX + textPadding
        }

        var tickCenterY: Float
        var textY: Float
        for (index in 0..tickCount) {

            tickCenterY = drawBounds.bottom - (axisStep * index)
            canvas.drawRect(
                tickLeftX,
                tickCenterY - (tickMarkThickness / 2),
                tickRightX,
                tickCenterY + (tickMarkThickness / 2),
                axisPaint
            )
            labelPaint.getTextBounds(TEXT_MEASUREMENT_CHAR, 0, TEXT_MEASUREMENT_CHAR.length, xTextBounds)
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
    }

    private fun getLabels(minY: Float, maxY: Float): List<String> {
        labels.clear()
        val step = (maxY - minY) / tickCount
        for (index in tickCount downTo 0) {
            labels += (maxY - (step * index)).toString()
        }
        return labels
    }

    override fun getSize(model: EntriesModel): Float {
        val widestTextWidth = getLabels(0f, model.maxY).maxOf { label ->
            labelPaint.measureText(label).also {
                Log.d("Test", "measuring $label, width=$it")
            }
        }
        return axisThickness + padding + tickMarkThickness + textPadding + widestTextWidth
    }

}