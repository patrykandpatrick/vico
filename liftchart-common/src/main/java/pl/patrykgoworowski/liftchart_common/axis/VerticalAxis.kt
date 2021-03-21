package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.Paint
import pl.patrykgoworowski.liftchart_common.DEF_AXIS_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_GUIDELINE_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_TICK_COMPONENT
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import kotlin.properties.Delegates.observable

class VerticalAxis(
    label: TextComponent = DEF_LABEL_COMPONENT,
    axis: RectComponent = DEF_AXIS_COMPONENT,
    tick: TickComponent = DEF_TICK_COMPONENT,
    guideline: GuidelineComponent = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<VerticalAxisPosition>(label, axis, tick, guideline) {

    private val labels = ArrayList<String>()

    override var isLTR: Boolean by observable(true) { _, _, value ->
        label.isLTR = isLTR
    }

    override var isVisible: Boolean = true

    var tickCount = 4

    override fun onDraw(canvas: Canvas, model: AxisModel, position: VerticalAxisPosition) {
        val isLeft = position.isLeft(isLTR)

        label.textAlign = if (isLeft) {
            Paint.Align.RIGHT
        } else {
            Paint.Align.LEFT
        }

        val labels = getLabels(model)
        val axisStep = bounds.height() / tickCount

        val tickLeftX = if (isLeft) {
            bounds.right - (axis.thickness + tick.length)
        } else {
            bounds.left
        }

        val tickRightX = if (isLeft) {
            bounds.right
        } else {
            bounds.left + axis.thickness + tick.length
        }

        val labelX = if (isLeft) {
            tickLeftX
        } else {
            tickRightX
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

            val guidelineLeft = dataSetBounds.left
            val guidelineRight = dataSetBounds.right

            if (guideline.shouldDraw &&
                guideline.fitsInHorizontal(
                    guidelineLeft,
                    guidelineRight,
                    tickCenterY,
                    dataSetBounds
                )
            ) {
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
        axis.drawVertical(
            canvas = canvas,
            top = bounds.top,
            bottom = bounds.bottom + axis.thickness,
            centerX = if (isLeft) {
                bounds.right - axis.thickness.half
            } else {
                bounds.left + axis.thickness.half
            }
        )
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

    override fun getDrawExtends(
        outDimensions: Dimensions<Float>,
        model: EntriesModel
    ): Dimensions<Float> {
        val labels = getLabels(model)
        if (labels.isEmpty()) return outDimensions.set(0f)

        fun getHalfLabelHeight(text: String): Float =
            label.getTextBounds(text).height().half * 1.2f

        return outDimensions.set(
            start = 0f,
            top = getHalfLabelHeight(labels.first()) - axisThickness,
            end = 0f,
            bottom = getHalfLabelHeight(labels.last())
        )
    }

    override fun getSize(model: EntriesModel, position: VerticalAxisPosition): Float {
        val widestTextWidth = getLabels(model).maxOf { label ->
            this.label.getWidth(label)
        }
        return axis.thickness.half + tick.length + widestTextWidth
    }

}