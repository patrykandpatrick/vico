package pl.patrykgoworowski.liftchart_compose.extension

import android.graphics.PointF
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.chartTouchEvent(
    setTouchPoint: (PointF?) -> Unit,
): Modifier =
    pointerInput(Unit, Unit) {
        detectTapGestures(
            onPress = {
                setTouchPoint(it.pointF)
                awaitRelease()
                setTouchPoint(null)
            }
        )
    }.pointerInput(Unit, Unit) {
        detectDragGestures(
            onDragEnd = { setTouchPoint(null) },
            onDragCancel = { setTouchPoint(null) },
            onDrag = { change, _ -> setTouchPoint(change.position.pointF) }
        )
    }

private val Offset.pointF: PointF
    get() = PointF(x, y)