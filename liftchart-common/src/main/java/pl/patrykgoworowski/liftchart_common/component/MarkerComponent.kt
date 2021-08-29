package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntryModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.averageOf
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero
import pl.patrykgoworowski.liftchart_common.marker.DefaultMarkerLabelFormatter
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.marker.MarkerLabelFormatter
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape

public open class MarkerComponent(
    private val label: TextComponent,
    private val indicator: Component,
    private val guideline: LineComponent,
    shape: MarkerCorneredShape,
    markerBackgroundColor: Int,
    dynamicShader: DynamicShader? = null
) : Marker, ShapeComponent<MarkerCorneredShape>(shape, markerBackgroundColor, dynamicShader) {

    private val markerTempBounds = RectF()

    private val markerHeight: Float
        get() = label.getHeight() + shape.tickSize.orZero

    public var indicatorSize: Float = 0f
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>
    ) {
        setParentBounds(bounds)
        applyShader(bounds)
        drawGuideline(canvas, bounds, markedEntries)

        markedEntries.forEachIndexed { _, model ->
            onApplyEntryColor?.invoke(model.color)
            indicator.draw(
                canvas,
                model.location.x - indicatorSize.half,
                model.location.y - indicatorSize.half,
                model.location.x + indicatorSize.half,
                model.location.y + indicatorSize.half,
            )

        }
        drawLabel(canvas, bounds, markedEntries, allEntries)
    }

    private fun drawLabel(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>,
    ) {
        val text = labelFormatter.getLabel(markedEntries, allEntries)
        val entryX = markedEntries.averageOf { it.location.x }
        val x = overrideXPositionToFit(entryX, bounds, text)

        label.drawText(
            canvas = canvas,
            text = text,
            textX = x,
            textY = bounds.top + label.allLinesHeight.half + label.padding.top - markerHeight,
        ) { textCanvas, left, top, right, bottom ->
            markerTempBounds.set(left, top, right, bottom)
            drawMarkerBackground(textCanvas, markerTempBounds, bounds, entryX)
        }
    }

    private fun drawMarkerBackground(
        canvas: Canvas,
        bounds: RectF,
        contentBounds: RectF,
        entryX: Float,
    ) {
        path.reset()
        shape.drawMarker(canvas, paint, path, bounds, contentBounds, entryX)
    }

    private fun overrideXPositionToFit(
        xPosition: Float,
        bounds: RectF,
        text: CharSequence,
    ): Float {
        val halfOfTextWidth = label.getWidth(text).half
        return when {
            xPosition - halfOfTextWidth < bounds.left -> bounds.left + halfOfTextWidth
            xPosition + halfOfTextWidth > bounds.right -> bounds.right - halfOfTextWidth
            else -> xPosition
        }
    }

    private fun drawGuideline(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
        guideline.setParentBounds(bounds)
        markedEntries
            .map { it.location.x }
            .toSet()
            .forEach { x ->
                guideline.drawVertical(
                    canvas,
                    bounds.top,
                    bounds.bottom,
                    x,
                )
            }
    }

    override fun getVerticalInsets(
        outDimensions: MutableDimensions,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): Dimensions =
        outDimensions.apply {
            top = markerHeight
        }

    override fun getHorizontalInsets(
        outDimensions: MutableDimensions,
        availableHeight: Float,
        model: EntryModel,
        dataSetModel: DataSetModel
    ): Dimensions = outDimensions

}