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
import pl.patrykgoworowski.liftchart_common.extension.orElse
import pl.patrykgoworowski.liftchart_common.extension.orZero

public open class AxisManager(
    public open var startAxis: VerticalAxisRenderer? = VerticalAxis(),
    public open var topAxis: HorizontalAxisRenderer? = null,
    public open var endAxis: VerticalAxisRenderer? = null,
    public open var bottomAxis: HorizontalAxisRenderer? = HorizontalAxis(),
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

    fun getAxesDimensions(
        outDimensions: MutableDimensions,
        model: EntriesModel,
        availableHeight: Int,
    ): Dimensions {
        startAxis?.getDrawExtends(startDimensions, model).orElse { startDimensions.set(0f) }
        topAxis?.getDrawExtends(topDimensions, model).orElse { topDimensions.set(0f) }
        endAxis?.getDrawExtends(endDimensions, model).orElse { endDimensions.set(0f) }
        bottomAxis?.getDrawExtends(bottomDimensions, model).orElse { bottomDimensions.set(0f) }

        outDimensions.top = maxOf(
            topAxis?.getHeight(TopAxis)?.toFloat().orZero,
            startDimensions.top,
            endDimensions.top
        )

        outDimensions.bottom = maxOf(
            bottomAxis?.getHeight(BottomAxis)?.toFloat().orZero,
            startDimensions.bottom,
            endDimensions.bottom
        )

        val height = (availableHeight - (outDimensions.bottom + outDimensions.top)).toInt()

        outDimensions.start = maxOf(
            startAxis?.getWidth(model, StartAxis, height)?.toFloat().orZero,
            topDimensions.start,
            bottomDimensions.start
        )

        outDimensions.end = maxOf(
            endAxis?.getWidth(model, EndAxis, height)?.toFloat().orZero,
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
                dataSetBounds.top + if (hasTopAxis) axis.tickThickness else -axis.tickThickness,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.tickThickness
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
                dataSetBounds.top + if (hasTopAxis) axis.tickThickness else -axis.tickThickness,
                horizontalAxisRightDrawBound,
                dataSetBounds.bottom + if (hasBottomAxis) 0f else axis.tickThickness
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
        startAxis?.draw(canvas, model, segmentProperties, StartAxis)
        topAxis?.draw(canvas, model, segmentProperties, TopAxis)
        endAxis?.draw(canvas, model, segmentProperties, EndAxis)
        bottomAxis?.draw(canvas, model, segmentProperties, BottomAxis)
    }

}