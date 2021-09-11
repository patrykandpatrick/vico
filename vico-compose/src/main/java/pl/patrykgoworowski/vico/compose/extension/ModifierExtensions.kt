/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.compose.extension

import android.graphics.PointF
import androidx.compose.foundation.gestures.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import pl.patrykgoworowski.vico.compose.gesture.OnZoom
import pl.patrykgoworowski.vico.compose.gesture.zoomable

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

