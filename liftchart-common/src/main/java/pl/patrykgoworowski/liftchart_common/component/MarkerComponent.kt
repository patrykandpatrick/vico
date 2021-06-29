package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.entry.DataEntry
import pl.patrykgoworowski.liftchart_common.extension.averageOf
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.orZero
import pl.patrykgoworowski.liftchart_common.marker.DefaultMarkerLabelFormatter
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.marker.MarkerLabelFormatter
import pl.patrykgoworowski.liftchart_common.path.DashedShape
import pl.patrykgoworowski.liftchart_common.path.corner.Corner
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape
import pl.patrykgoworowski.liftchart_common.path.corner.RoundedCornerTreatment
import pl.patrykgoworowski.liftchart_common.path.pillShape
import pl.patrykgoworowski.liftchart_common.path.rectShape

public open class MarkerComponent(
    private val label: TextComponent = TextComponent(),
    private val guideline: RectComponent = RectComponent(
        0x4A000000, 2f.dp, DashedShape(
            pillShape(), 8f.dp, 4f.dp
        )
    ),
    private val background: ShapeComponent = ShapeComponent(rectShape(), Color.WHITE)
) : Marker {

    init {
        background.apply {
            color = Color.WHITE
            shape = MarkerCorneredShape(
                topLeft = Corner.Relative(100, RoundedCornerTreatment),
                topRight = Corner.Relative(100, RoundedCornerTreatment),
                bottomLeft = Corner.Relative(100, RoundedCornerTreatment),
                bottomRight = Corner.Relative(100, RoundedCornerTreatment),
            )
            paint.setShadowLayer(4f.dp, 0f, 2f.dp, 0x8A000000.toInt())
        }
        label.apply {
            background = null
            setPadding(8f.dp, 4f.dp)
        }
    }

    val outer = ShapeComponent(pillShape())
    val center = ShapeComponent(pillShape()).apply {
        setShadow(4f.dp, 0f, 1f.dp, 0x4A000000)
    }
    val dot = OverlayingComponent(
        outer,
        OverlayingComponent(
            center,
            ShapeComponent(pillShape(), Color.WHITE),
            6f.dp,
        ),
        11f.dp
    )

    private val markerHeight: Float
        get() = label.getHeight() + (background.shape as? MarkerCorneredShape)?.tickSize.orZero

    public var entryTipSize: Float = 42f.dp
    public var onApplyEntryColor: ((entryColor: Int) -> Unit)? = null
    public var labelFormatter: MarkerLabelFormatter = DefaultMarkerLabelFormatter

    override fun draw(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
        allEntries: List<DataEntry>
    ) {
        drawGuideline(canvas, bounds, markedEntries)

        markedEntries.forEachIndexed { index, model ->
            onApplyEntryColor?.invoke(model.color)

            outer.color = model.color
            outer.paint.alpha = 64
            center.color = model.color
            dot.draw(
                canvas,
                model.location.x - entryTipSize.half,
                model.location.y - entryTipSize.half,
                model.location.x + entryTipSize.half,
                model.location.y + entryTipSize.half,
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
        val x = markedEntries.averageOf { it.location.x }
        label.drawText(
            canvas = canvas,
            text = labelFormatter.getLabel(markedEntries, allEntries),
            textX = x,
            textY = bounds.top + label.allLinesHeight.half + label.padding.top - markerHeight,
        ) { textCanvas, left, top, right, bottom ->
            background.draw(textCanvas, left, top, right, bottom)
        }
    }

    private fun drawGuideline(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<Marker.EntryModel>,
    ) {
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

    override fun getInsets(outDimensions: MutableDimensions, model: EntriesModel): Dimensions =
        outDimensions.apply {
            top = markerHeight
        }

}