package pl.patrykgoworowski.liftchart_common.data_set.axis

import pl.patrykgoworowski.liftchart_common.AnyEntry

interface AxisModel {
    val minX: Float
    val maxX: Float
    val minY: Float
    val maxY: Float
    val step: Float
    val xSegmentWidth: Float
    val xSegmentSpacing: Float
    val entries: List<AnyEntry>
}