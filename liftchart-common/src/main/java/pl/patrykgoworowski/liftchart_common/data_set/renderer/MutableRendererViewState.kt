package pl.patrykgoworowski.liftchart_common.data_set.renderer

import android.graphics.PointF

data class MutableRendererViewState(
    override var markerTouchPoint: PointF? = null,
    override var horizontalScroll: Float = 0f,
): RendererViewState
