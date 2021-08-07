package pl.patrykgoworowski.liftchart_compose.extension

import android.graphics.PointF
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

inline fun Modifier.runIf(predicate: Boolean, action: Modifier.() -> Modifier) =
    if (predicate) action() else this

inline fun Modifier.runIf(
    predicate: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier,
) = if (predicate) this.ifTrue() else this.ifFalse()

fun Modifier.chartTouchEvent(
    isHorizontalScrollEnabled: Boolean = false,
    setTouchPoint: (PointF?) -> Unit,
    scrollableState: ScrollableState?,
): Modifier {

    return pointerInput(Unit, Unit) {
        detectTapGestures(
            onPress = {
                setTouchPoint(it.pointF)
                awaitRelease()
                setTouchPoint(null)
            }
        )
    }.runIf(isHorizontalScrollEnabled,
        ifTrue = {
            if (scrollableState == null) return@runIf this
            scrollable(
                state = scrollableState,
                orientation = Orientation.Horizontal,
            )
        },
        ifFalse = {
            pointerInput(Unit, Unit) {
                detectDragGestures(
                    onDragEnd = { setTouchPoint(null) },
                    onDragCancel = { setTouchPoint(null) },
                    onDrag = { change, _ -> setTouchPoint(change.position.pointF) }
                )
            }
        })
}


private val Offset.pointF: PointF
    get() = PointF(x, y)