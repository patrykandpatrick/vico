package pl.patrykgoworowski.liftchart_common.axis

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.axis.component.TickComponent
import pl.patrykgoworowski.liftchart_common.axis.formatter.AxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.LineComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.segment.SegmentProperties
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter

interface AxisRenderer<Position : AxisPosition> : BoundsAware, DataSetInsetter {

    val position: Position
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
    var axis: LineComponent?
    var tick: TickComponent?
    var guideline: LineComponent?
    var isLTR: Boolean
    var isVisible: Boolean
    var valueFormatter: AxisValueFormatter

    fun draw(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
    ) {
        if (isVisible) {
            onDraw(canvas, model, dataSetModel, segmentProperties)
        }
    }

    fun onDraw(
        canvas: Canvas,
        model: EntriesModel,
        dataSetModel: DataSetModel,
        segmentProperties: SegmentProperties,
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

    fun getDesiredWidth(
        model: EntriesModel,
        dataSetModel: DataSetModel,
    ): Int

    fun getDesiredHeight(): Int

}