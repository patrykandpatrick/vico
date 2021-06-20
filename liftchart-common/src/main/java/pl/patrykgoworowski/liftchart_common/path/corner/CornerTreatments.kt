package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Path
import android.graphics.RectF

object SharpCornerTreatment : CornerTreatment {

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) = when(cornerLocation) {
        CornerLocation.TopLeft -> {
            path.lineTo(x1, y2)
        }
        CornerLocation.TopRight -> {
            path.lineTo(x2, y1)
        }
        CornerLocation.BottomRight -> {
            path.lineTo(x1, y2)
        }
        CornerLocation.BottomLeft -> {
            path.lineTo(x2, y1)
        }
    }

}

object CutCornerTreatment : CornerTreatment {

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) {
        path.lineTo(x1, y1)
        path.lineTo(x2, y2)
    }

}

object RoundedCornerTreatment : CornerTreatment {

    private val tempRect = RectF()

    override fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path
    ) {
        val startAngle: Float
        when(cornerLocation) {
            CornerLocation.TopLeft -> {
                startAngle = 180f
                tempRect.set(x1, y2, (x2 * 2) - x1, (y1 * 2) - y2)
            }
            CornerLocation.TopRight -> {
                startAngle = 270f
                tempRect.set((x1 * 2) - x2, y1, x2, (y2 * 2) - y1)
            }
            CornerLocation.BottomRight -> {
                startAngle = 0f
                tempRect.set((x2 * 2) - x1, (y1 * 2) - y2, x1, y2)
            }
            CornerLocation.BottomLeft -> {
                startAngle = 90f
                tempRect.set(x2, (y2 * 2) - y1, (x1 * 2) - x2, y1)
            }
        }
        path.arcTo(tempRect, startAngle, 90f)
    }

}