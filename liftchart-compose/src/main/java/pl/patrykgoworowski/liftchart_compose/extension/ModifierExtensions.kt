package pl.patrykgoworowski.liftchart_compose.extension

import android.graphics.PointF
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import pl.patrykgoworowski.liftchart_compose.gesture.OnZoom
import pl.patrykgoworowski.liftchart_compose.gesture.zoomable

fun Modifier.chartTouchEvent(
    setTouchPoint: (PointF?) -> Unit,
    scrollableState: ScrollableState?,
    onZoom: OnZoom?,
): Modifier = pointerInput(Unit, Unit) {
    detectTapGestures(
        onPress = {
            setTouchPoint(it.pointF)
            awaitRelease()
            setTouchPoint(null)
        }
    )
}
    .then(onZoom?.let(Modifier::zoomable) ?: Modifier)
    .then(scrollableState?.let { state ->
        scrollable(
            state = state,
            orientation = Orientation.Horizontal,
        )
    } ?: pointerInput(Unit, Unit) {
        detectDragGestures(
            onDragEnd = { setTouchPoint(null) },
            onDragCancel = { setTouchPoint(null) },
            onDrag = { change, _ -> setTouchPoint(change.position.pointF) }
        )
    })

private val Offset.pointF: PointF
    get() = PointF(x, y)

