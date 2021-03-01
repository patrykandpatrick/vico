package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.extension.half
import kotlin.properties.Delegates.observable

class VerticalAxis(
    override val position: VerticalAxisPosition,
    label: TextComponent = TextComponent(),
    axis: RectComponent = RectComponent(Color.BLUE, 4f),
    tick: TickComponent = TickComponent(Color.BLUE, 4f),
    guideline: GuidelineComponent = GuidelineComponent(Color.GRAY, 4f),
) : BaseLabeledAxisRenderer(position, label, axis, tick, guideline) {

    private val isLeft: Boolean
        get() = (position == StartAxis && isLTR) || (position == EndAxis && !isLTR)

    private val labels = ArrayList<String>()

    override var isLTR: Boolean by observable(true) { _, _, isLTR ->
        label.textAlign = if (isLeft) {
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

            labels.getOrNull(index)?.let { label ->
                this.label.drawTextCenteredVertically(
                    canvas,
                    label,
                    labelX,
                    tickCenterY
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
            this.label.getWidth(label)
        }
        return axis.thickness.half + tick.length + textPadding + widestTextWidth
    }

}