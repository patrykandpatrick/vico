package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.dimensions.floatDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero

public open class AxisManager(
    public open var startAxis: AxisRenderer<AxisPosition.Vertical>? = VerticalAxis(),
    public open var topAxis: AxisRenderer<AxisPosition.Horizontal>? = null,
    public open var endAxis: AxisRenderer<AxisPosition.Vertical>? = null,
    public open var bottomAxis: AxisRenderer<AxisPosition.Horizontal>? = HorizontalAxis(),
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

    public val leftAxis: AxisRenderer<AxisPosition.Vertical>?
        get() = if (isLTR) startAxis else endAxis

    public val rightAxis: AxisRenderer<AxisPosition.Vertical>?
        get() = if (isLTR) endAxis else startAxis

    fun getAxesDimensions(
        outDimensions: MutableDimensions,
        model: EntriesModel,
        availableHeight: Int,
    ): Dimensions {

        resetDimensions()

        startAxis?.getInsets(startDimensions, model)
        topAxis?.getInsets(topDimensions, model)
        endAxis?.getInsets(endDimensions, model)
        bottomAxis?.getInsets(bottomDimensions, model)

        outDimensions.top = maxOf(
            topAxis?.getDesiredHeight(AxisPosition.Horizontal.Top)?.toFloat().orZero,
            startDimensions.top,
            endDimensions.top
        )

        outDimensions.bottom = maxOf(
            bottomAxis?.getDesiredHeight(AxisPosition.Horizontal.Bottom)?.toFloat().orZero,
            startDimensions.bottom,
            endDimensions.bottom
        )

        val height = (availableHeight - (outDimensions.bottom + outDimensions.top)).toInt()

        outDimensions.start = maxOf(
            startAxis?.getDesiredWidth(model, AxisPosition.Vertical.Start, height)?.toFloat().orZero,
            topDimensions.start,
            bottomDimensions.start
        )

        outDimensions.end = maxOf(
            endAxis?.getDesiredWidth(model, AxisPosition.Vertical.End, height)?.toFloat().orZero,
            topDimensions.end,
            bottomDimensions.end
        )

        return outDimensions
    }

    fun setAxesBounds(
        contentBounds: RectF,
        dataSetBounds: RectF,
        axesDimensions: Dimensions,
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
                dataSetBounds.top + axis.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.maxAnyAxisLineThickness
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
                dataSetBounds.top + axis.maxAnyAxisLineThickness * if (hasTopAxis) 1 else -1,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.maxAnyAxisLineThickness
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
                dataSetBounds.top + if (hasTopAxis) axis.axisThickness else 0f,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom
            )
        }
    }

    fun draw(
        canvas: Canvas,
        model: EntriesModel,
        segmentProperties: SegmentProperties,
    ) {
        startAxis?.draw(canvas, model, segmentProperties, AxisPosition.Vertical.Start)
        topAxis?.draw(canvas, model, segmentProperties, AxisPosition.Horizontal.Top)
        endAxis?.draw(canvas, model, segmentProperties, AxisPosition.Vertical.End)
        bottomAxis?.draw(canvas, model, segmentProperties, AxisPosition.Horizontal.Bottom)
    }

    private fun resetDimensions() {
        startDimensions.set(0f)
        topDimensions.set(0f)
        endDimensions.set(0f)
        bottomDimensions.set(0f)
    }

}