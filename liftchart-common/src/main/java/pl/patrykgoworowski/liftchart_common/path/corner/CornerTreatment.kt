package pl.patrykgoworowski.liftchart_common.path.corner

import android.graphics.Path

public interface CornerTreatment {

    public fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path,
    )

}