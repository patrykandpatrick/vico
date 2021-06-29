package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Path
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.extension.dp

public open class MarkerCorneredShape(
    topLeft: Corner,
    topRight: Corner,
    bottomRight: Corner,
    bottomLeft: Corner,
    public val tickSize: Float = 6f.dp,
) : CorneredShape(
    topLeft, topRight, bottomRight, bottomLeft
) {

    override fun onShapePathCreated(path: Path, bounds: RectF) {
        val centerX = bounds.centerX()
        path.moveTo(centerX - tickSize, bounds.bottom)
        path.lineTo(centerX, bounds.bottom + tickSize)
        path.lineTo(centerX + tickSize, bounds.bottom)
        path.close()
    }

}