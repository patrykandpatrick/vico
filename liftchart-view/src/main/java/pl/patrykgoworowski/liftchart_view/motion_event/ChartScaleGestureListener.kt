package pl.patrykgoworowski.liftchart_view.motion_event

import android.graphics.RectF
import android.view.ScaleGestureDetector

class ChartScaleGestureListener(
    private val getChartBounds: () -> RectF?,
    private val onZoom: (focusX: Float, focusY: Float, zoomChange: Float) -> Unit
) : ScaleGestureDetector.OnScaleGestureListener {

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        onZoom(detector.focusX, detector.focusY, detector.scaleFactor)
        return true
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean =
        getChartBounds()?.contains(detector.focusX, detector.focusY) == true

    override fun onScaleEnd(detector: ScaleGestureDetector) {}
}