package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.component.GuidelineComponent
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.component.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions

interface AxisRenderer<Position : AxisPosition> : BoundsAware {

    val dataSetBounds: RectF
    val axisThickness: Float
    val tickThickness: Float
    val guidelineThickness: Float
    val tickLength: Float

    public val maxAnyAxisLineThickness: Float
        get() = maxOf(axisThickness, tickThickness, guidelineThickness)

    public val labelLineHeight: Int
        get() = label?.lineHeight ?: 0

    public val labelAllLinesHeight: Int
        get() = label?.allLinesHeight ?: 0

    var label: TextComponent?
    var axis: RectComponent?
    var tick: TickComponent?
    var guideline: GuidelineComponent?
    var isLTR: Boolean
    var isVisible: Boolean
    var valueFormatter: AxisValueFormatter

    fun draw(
        canvas: Canvas,
        model: EntriesModel,
        segmentProperties: SegmentProperties,
        position: Position,
    ) {
        if (isVisible) {
            onDraw(canvas, model, segmentProperties, position)
        }
    }

    fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        segmentProperties: SegmentProperties,
        position: Position,
    )

    fun setDataSetBounds(
        left: Number,
        top: Number,
        right: Number,
        bottom: Number
    )

    fun setDataSetBounds(bounds: RectF) =
        setDataSetBounds(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )

    fun getDrawExtends(
        outDimensions: MutableDimensions,
        model: EntriesModel,
    ): Dimensions

}