package pl.patrykgoworowski.liftchart_core.data_set

import pl.patrykgoworowski.liftchart_core.data_set.segment.SegmentSpec
import pl.patrykgoworowski.liftchart_core.entry.DataEntry

typealias AnyEntry = DataEntry<*, *>

abstract class BaseDataSet<Color, Canvas, Bounds, T: AnyEntry, C: Collection<T>>(
    color: Color,
    segmentSpec: SegmentSpec,
    entryCollection: EntryCollection<T, C>
) : EntryCollection<T, C> by entryCollection {

    var color: Color = color

    var segmentSpec: SegmentSpec = segmentSpec

    abstract fun draw(canvas: Canvas, bounds: Bounds, segmentWidth: Float, animationOffset: Float)

}