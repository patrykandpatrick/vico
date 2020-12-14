package pl.patrykgoworowski.liftchart_core.data_set

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

abstract class BaseDataSet<Spec: SegmentSpec, C: Collection<AnyEntry>>(
    color: Int = Color.BLACK,
    segmentSpec: Spec,
    entryManager: EntryManager<C>
) : EntryManager<C> by entryManager {

    var color: Int = color

    var segmentSpec: Spec = segmentSpec

    abstract fun draw(canvas: Canvas, bounds: RectF, segmentWidth: Float, animationOffset: Float)

}