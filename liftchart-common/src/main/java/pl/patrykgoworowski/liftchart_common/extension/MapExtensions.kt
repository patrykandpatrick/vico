package pl.patrykgoworowski.liftchart_common.extension

import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position
import java.util.*

public operator fun EnumMap<Position, AxisRenderer>.plusAssign(axisRenderer: AxisRenderer) {
    put(axisRenderer.getAxisPosition(), axisRenderer)
}

public operator fun EnumMap<Position, AxisRenderer>.minusAssign(axisRenderer: AxisRenderer) {
    remove(axisRenderer.getAxisPosition())
}