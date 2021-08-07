package pl.patrykgoworowski.liftchart_common.data_set.renderer

import android.graphics.PointF

interface RendererViewState {

    val markerTouchPoint: PointF?
    val horizontalScroll: Float

    operator fun component1(): PointF? = markerTouchPoint
    operator fun component2(): Float = horizontalScroll

}