package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.Paint
import pl.patrykgoworowski.liftchart_common.DEF_AXIS_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_GUIDELINE_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_COMPONENT
import pl.patrykgoworowski.liftchart_common.DEF_TICK_COMPONENT
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero
import pl.patrykgoworowski.liftchart_common.extension.orZeroInt

class VerticalAxis(
    label: TextComponent? = DEF_LABEL_COMPONENT,
    axis: RectComponent? = DEF_AXIS_COMPONENT,
    tick: TickComponent? = DEF_TICK_COMPONENT,
    guideline: GuidelineComponent? = DEF_GUIDELINE_COMPONENT,
) : BaseLabeledAxisRenderer<VerticalAxisPosition>(label, axis, tick, guideline),
    VerticalAxisRenderer {

    override var isVisible: Boolean = true

    var tickCount = 4

    override fun onDraw(canvas: Canvas, model: EntriesModel, position: VerticalAxisPosition) {
        val isLeft = position.isLeft(isLTR)

        label?.textAlign = if (isLeft) {
            Paint.Align.RIGHT
        } else {
            Paint.Align.LEFT
        }

        val labels = getLabels(model)
        val axisStep = bounds.height() / tickCount

        val tickLeftX = if (isLeft) {
            bounds.right - (axisThickness + tickLength)
        } else {
            bounds.left
        }

        val tickRightX = if (isLeft) {
            bounds.right
        } else {
            bounds.left + axisThickness + tickLength
        }

        val labelX = if (isLeft) {
            tickLeftX
        } else {
            tickRightX
        }

        var tickCenterY: Float

        for (index in 0..tickCount) {

            tickCenterY = bounds.bottom - (axisStep * index) + (axisThickness / 2)

            tick?.drawHorizontal(
                canvas = canvas,
                left = tickLeftX,
                right = tickRightX,
                tickCenterY
            )

            val guidelineLeft = dataSetBounds.left
            val guidelineRight = dataSetBounds.right

            guideline?.takeIf {
                it.shouldDraw &&
                        it.fitsInHorizontal(
                            guidelineLeft,
                            guidelineRight,
                            tickCenterY,
                            dataSetBounds
                        )
            }?.drawHorizontal(
                canvas = canvas,
                left = dataSetBounds.left,
                right = dataSetBounds.right,
                centerY = tickCenterY
            )

            labels.getOrNull(index)?.let { label ->
                this.label?.drawTextVertically(
                    canvas,
                    label,
                    labelX,
                    tickCenterY,
                    TextComponent.VerticalPosition.Center,
                )
            }
        }
        axis?.drawVertical(
            canvas = canvas,
            top = bounds.top,
            bottom = bounds.bottom + axisThickness,
            centerX = if (isLeft) {
                bounds.right - axisThickness.half
            } else {
                bounds.left + axisThickness.half
            }
        )
        label?.clearLayoutCache()
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
            label?.getTextBounds(text)?.height()?.half.orZeroInt * 1.2f

        return outDimensions.set(
            start = 0f,
            top = getHalfLabelHeight(labels.first()) - axisThickness,
            end = 0f,
            bottom = getHalfLabelHeight(labels.last())
        )
    }

    override fun getWidth(
        model: EntriesModel,
        position: VerticalAxisPosition,
    ): Float {
        val widestTextWidth = label?.let { label ->
            getLabels(model).maxOf { labelText ->
                label.getWidth(labelText)
            }
        }.orZero
        return axisThickness.half + tickLength + widestTextWidth
    }

}