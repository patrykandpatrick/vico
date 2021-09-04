package pl.patrykgoworowski.liftchart_common.marker

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter
import pl.patrykgoworowski.liftchart_common.entry.DataEntry

public interface Marker : DataSetInsetter {

    public fun draw(
        canvas: Canvas,
        bounds: RectF,
        markedEntries: List<EntryModel>,
    )

    class EntryModel(
        val location: PointF,
        val entry: DataEntry,
        val color: Int,
    )
}
