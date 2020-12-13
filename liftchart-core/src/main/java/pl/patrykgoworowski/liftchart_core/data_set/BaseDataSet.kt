package pl.patrykgoworowski.liftchart_core.data_set

import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

abstract class BaseDataSet<Color, Canvas, Bounds, C: Collection<AnyEntry>>(
    color: Color,
    segmentSpec: SegmentSpec,
    entryCollection: EntryCollection<C>
) : EntryCollection<C> by entryCollection {

    var color: Color = color

    var segmentSpec: SegmentSpec = segmentSpec

    abstract fun draw(canvas: Canvas, bounds: Bounds, segmentWidth: Float, animationOffset: Float)

}