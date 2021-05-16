package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.floatDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orElse
import pl.patrykgoworowski.liftchart_common.extension.orZero

@OptIn(ExperimentalStdlibApi::class)
public open class AxisManager(
    public open var startAxis: AxisRenderer<VerticalAxisPosition>? = VerticalAxis(),
    public open var topAxis: AxisRenderer<HorizontalAxisPosition>? = null,
    public open var endAxis: AxisRenderer<VerticalAxisPosition>? = null,
    public open var bottomAxis: AxisRenderer<HorizontalAxisPosition>? = HorizontalAxis(),
) {

    private val startDimensions = floatDimensions()
    private val topDimensions = floatDimensions()
    private val endDimensions = floatDimensions()
    private val bottomDimensions = floatDimensions()

    private val hasLeftAxis: Boolean
        get() = leftAxis != null

    private val hasTopAxis: Boolean
        get() = topAxis != null

    private val hasRightAxis: Boolean
        get() = rightAxis != null

    private val hasBottomAxis: Boolean
        get() = bottomAxis != null

    public var isLTR: Boolean = true

    public val leftAxis: AxisRenderer<VerticalAxisPosition>?
        get() = if (isLTR) startAxis else endAxis

    public val rightAxis: AxisRenderer<VerticalAxisPosition>?
        get() = if (isLTR) endAxis else startAxis

    fun getAxisWidth(model: AxisModel): Float =
        startAxis?.getSize(model, StartAxis).orZero +
                endAxis?.getSize(model, EndAxis).orZero

    fun getAxesDimensions(
        outDimensions: Dimensions<Float>,
        axisModel: AxisModel,
    ): Dimensions<Float> {
        startAxis?.getDrawExtends(startDimensions, axisModel).orElse { startDimensions.set(0f) }
        topAxis?.getDrawExtends(topDimensions, axisModel).orElse { topDimensions.set(0f) }
        endAxis?.getDrawExtends(endDimensions, axisModel).orElse { endDimensions.set(0f) }
        bottomAxis?.getDrawExtends(bottomDimensions, axisModel).orElse { bottomDimensions.set(0f) }

        outDimensions.start = maxOf(
            startAxis?.getSize(axisModel, StartAxis).orZero,
            topDimensions.start,
            bottomDimensions.start
        )
        outDimensions.top = maxOf(
            topAxis?.getSize(axisModel, TopAxis).orZero,
            startDimensions.top,
            endDimensions.top
        )
        outDimensions.end = maxOf(
            endAxis?.getSize(axisModel, EndAxis).orZero,
            topDimensions.end,
            bottomDimensions.end
        )
        outDimensions.bottom = maxOf(
            bottomAxis?.getSize(axisModel, BottomAxis).orZero,
            startDimensions.bottom,
            endDimensions.bottom
        )
        return outDimensions
    }

    fun setAxesBounds(
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions<Float>,
    ) {

        val horizontalAxisLeftDrawBound = dataSetBounds.left + (leftAxis?.axisThickness?.half
            ?: -axesDimensions.getLeft(isLTR))

        val horizontalAxisRightDrawBound = dataSetBounds.right - (rightAxis?.axisThickness?.half
            ?: -axesDimensions.getRight(isLTR))

        startAxis?.let { axis ->
            axis.setBounds(
                left = if (isLTR) contentBounds.left else contentBounds.right - axesDimensions.end,
                top = contentBounds.top + axesDimensions.top,
                right = if (isLTR)
                    contentBounds.left + axesDimensions.start + axis.axisThickness.half
                else
                    contentBounds.right,
                bottom = contentBounds.bottom - axesDimensions.bottom
            )
            axis.dataSetBounds.set(
                horizontalAxisLeftDrawBound,
                dataSetBounds.top + if (hasTopAxis) axis.tick.thickness else -axis.tick.thickness,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.tick.thickness
            )
        }
        topAxis?.let { axis ->
            axis.setBounds(
                left = contentBounds.left + axesDimensions.start,
                top = contentBounds.top,
                right = contentBounds.right - axesDimensions.end,
                bottom = contentBounds.top + axesDimensions.top
            )
            axis.dataSetBounds.set(
                horizontalAxisLeftDrawBound,
                dataSetBounds.top,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom
            )
        }
        endAxis?.let { axis ->
            axis.setBounds(
                left = if (isLTR)
                    contentBounds.right - (axesDimensions.end + axis.axisThickness.half)
                else
                    contentBounds.left,
                top = contentBounds.top + axesDimensions.top,
                right = if (isLTR)
                    contentBounds.right
                else
                    contentBounds.left + axesDimensions.end,
                bottom = contentBounds.bottom - axesDimensions.bottom
            )
            axis.dataSetBounds.set(
                horizontalAxisLeftDrawBound,
                dataSetBounds.top + if (hasTopAxis) axis.tick.thickness else -axis.tick.thickness,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.tick.thickness
            )
        }
        bottomAxis?.let { axis ->
            axis.setBounds(
                left = contentBounds.left + axesDimensions.start,
                top = contentBounds.bottom - axesDimensions.bottom,
                right = contentBounds.right - axesDimensions.end,
                bottom = contentBounds.bottom
            )
            axis.dataSetBounds.set(
                horizontalAxisLeftDrawBound,
                dataSetBounds.top,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom
            )
        }
    }

    fun draw(
        canvas: Canvas,
        axisModel: AxisModel,
    ) {
        startAxis?.draw(canvas, axisModel, StartAxis)
        topAxis?.draw(canvas, axisModel, TopAxis)
        endAxis?.draw(canvas, axisModel, EndAxis)
        bottomAxis?.draw(canvas, axisModel, BottomAxis)
    }

}