package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.floatDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orElse
import pl.patrykgoworowski.liftchart_common.extension.orZero

@OptIn(ExperimentalStdlibApi::class)
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

    fun getAxisWidth(model: EntriesModel): Float =
        startAxis?.getWidth(model, StartAxis).orZero +
                endAxis?.getWidth(model, EndAxis).orZero

    fun getAxesDimensions(
        outDimensions: Dimensions<Float>,
        model: EntriesModel,
        contentBounds: RectF,
    ): Dimensions<Float> {
        startAxis?.getDrawExtends(startDimensions, model).orElse { startDimensions.set(0f) }
        topAxis?.getDrawExtends(topDimensions, model).orElse { topDimensions.set(0f) }
        endAxis?.getDrawExtends(endDimensions, model).orElse { endDimensions.set(0f) }
        bottomAxis?.getDrawExtends(bottomDimensions, model).orElse { bottomDimensions.set(0f) }

        outDimensions.start = maxOf(
            startAxis?.getWidth(model, StartAxis).orZero,
            topDimensions.start,
            bottomDimensions.start
        )

        outDimensions.end = maxOf(
            endAxis?.getWidth(model, EndAxis).orZero,
            topDimensions.end,
            bottomDimensions.end
        )

        val width = contentBounds.width() - (outDimensions.start + outDimensions.end)

        outDimensions.top = maxOf(
            topAxis?.getHeight(model, TopAxis, width).orZero,
            startDimensions.top,
            endDimensions.top
        )

        outDimensions.bottom = maxOf(
            bottomAxis?.getHeight(model, BottomAxis, width).orZero,
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
        model: EntriesModel,
    ) {
        startAxis?.draw(canvas, model, StartAxis)
        topAxis?.draw(canvas, model, TopAxis)
        endAxis?.draw(canvas, model, EndAxis)
        bottomAxis?.draw(canvas, model, BottomAxis)
    }

}