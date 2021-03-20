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

    private val tempDimensions = floatDimensions()
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

    @Suppress("UNCHECKED_CAST")
    val axes: Map<AxisPosition, AxisRenderer<AxisPosition>>
        get() = buildMap {
            startAxis?.let { put(StartAxis, it as AxisRenderer<AxisPosition>) }
            topAxis?.let { put(TopAxis, it as AxisRenderer<AxisPosition>) }
            endAxis?.let { put(EndAxis, it as AxisRenderer<AxisPosition>) }
            bottomAxis?.let { put(BottomAxis, it as AxisRenderer<AxisPosition>) }
        }

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
        axisModel: AxisModel,
    ) {
        getAxesDimensions(tempDimensions, axisModel)

        val horizontalAxisLeftDrawBound = dataSetBounds.left + (leftAxis?.axisThickness?.half
            ?: -tempDimensions.getLeft(isLTR))

        val horizontalAxisRightDrawBound = dataSetBounds.right - (rightAxis?.axisThickness?.half
            ?: -tempDimensions.getRight(isLTR))

        startAxis?.let { axis ->
            axis.setBounds(
                left = if (isLTR) contentBounds.left else contentBounds.right - tempDimensions.end,
                top = contentBounds.top + tempDimensions.top,
                right = if (isLTR)
                    contentBounds.left + tempDimensions.start + axis.axisThickness.half
                else
                    contentBounds.right,
                bottom = contentBounds.bottom - tempDimensions.bottom
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
                left = contentBounds.left + tempDimensions.start,
                top = contentBounds.top,
                right = contentBounds.right - tempDimensions.end,
                bottom = contentBounds.top + tempDimensions.top
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
                    contentBounds.right - (tempDimensions.end + axis.axisThickness.half)
                else
                    contentBounds.left,
                top = contentBounds.top + tempDimensions.top,
                right = if (isLTR)
                    contentBounds.right
                else
                    contentBounds.left + tempDimensions.end,
                bottom = contentBounds.bottom - tempDimensions.bottom
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
                left = contentBounds.left + tempDimensions.start,
                top = contentBounds.bottom - tempDimensions.bottom,
                right = contentBounds.right - tempDimensions.end,
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
        axes.forEach { (position, axis) ->
            axis.draw(canvas, axisModel, position)
        }
    }

}