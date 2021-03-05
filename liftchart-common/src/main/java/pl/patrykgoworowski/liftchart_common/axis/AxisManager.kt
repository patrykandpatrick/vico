package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.floatDimensions
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero

@OptIn(ExperimentalStdlibApi::class)
public open class AxisManager(
    public open var startAxis: AxisRenderer<VerticalAxisPosition>? = VerticalAxis(),
    public open var topAxis: AxisRenderer<HorizontalAxisPosition>? = HorizontalAxis(),
    public open var endAxis: AxisRenderer<VerticalAxisPosition>? = null,
    public open var bottomAxis: AxisRenderer<HorizontalAxisPosition>? = null,
) {

    private val axesDimensions: Dimensions<Float> = floatDimensions()
    
    val axes: Map<AxisPosition, AxisRenderer<AxisPosition>>
        get() = buildMap {
            startAxis?.let { put(StartAxis, it as AxisRenderer<AxisPosition>) }
            topAxis?.let { put(TopAxis, it as AxisRenderer<AxisPosition>) }
            endAxis?.let { put(EndAxis, it as AxisRenderer<AxisPosition>) }
            bottomAxis?.let { put(BottomAxis, it as AxisRenderer<AxisPosition>) }
        }

    fun getLeftAxis(isLTR: Boolean): AxisRenderer<VerticalAxisPosition>? =
        if (isLTR) startAxis else endAxis

    fun getRightAxis(isLTR: Boolean): AxisRenderer<VerticalAxisPosition>? =
        if (isLTR) endAxis else startAxis

    fun getAxisWidth(model: AxisModel): Float =
        startAxis?.getSize(model, StartAxis).orZero + endAxis?.getSize(model, EndAxis).orZero

    fun getAxesDimensions(
        outDimensions: Dimensions<Float>,
        axisModel: AxisModel,
    ): Dimensions<Float> {
        outDimensions.start = startAxis?.getSize(axisModel, StartAxis) ?: 0f
        outDimensions.top = topAxis?.getSize(axisModel, TopAxis) ?: 0f
        outDimensions.end = endAxis?.getSize(axisModel, EndAxis) ?: 0f
        outDimensions.bottom = bottomAxis?.getSize(axisModel, BottomAxis) ?: 0f
        return outDimensions
    }

    fun setAxesBounds(
        contentBounds: RectF,
        dataSetBounds: RectF,
        axisModel: AxisModel,
        isLTR: Boolean
    ) {
        getAxesDimensions(axesDimensions, axisModel)

        axes.forEach { (_, axis) -> axis.dataSetBounds.set(dataSetBounds) }
        
        startAxis?.let { axis ->
            axis.setBounds(
                left = if (isLTR) contentBounds.left else contentBounds.right - axesDimensions.end,
                top = contentBounds.top + axesDimensions.top,
                right = if (isLTR) contentBounds.left + axesDimensions.start + axis.axisThickness.half else contentBounds.right,
                bottom = contentBounds.bottom - axesDimensions.bottom
            )
        }
        topAxis?.setBounds(
            left = contentBounds.left + axesDimensions.start,
            top = contentBounds.top,
            right = contentBounds.right - axesDimensions.end,
            bottom = contentBounds.top + axesDimensions.top
        )
        endAxis?.let { axis ->
            axis.setBounds(
                left = if (isLTR) contentBounds.right - (axesDimensions.end + axis.axisThickness.half) else contentBounds.left,
                top = contentBounds.top + axesDimensions.top,
                right = if (isLTR) contentBounds.right else contentBounds.left + axesDimensions.end,
                bottom = contentBounds.bottom - axesDimensions.bottom
            )
        }
        bottomAxis?.setBounds(
            left = contentBounds.left + axesDimensions.start,
            top = contentBounds.bottom - axesDimensions.bottom,
            right = contentBounds.right - axesDimensions.end,
            bottom = contentBounds.bottom
        )
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