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

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import pl.patrykgoworowski.vico.compose.gesture.OnZoom
import pl.patrykgoworowski.vico.compose.gesture.zoomable
import pl.patrykgoworowski.vico.core.model.Point

internal fun Modifier.chartTouchEvent(
    setTouchPoint: (Point?) -> Unit,
    scrollableState: ScrollableState?,
    onZoom: OnZoom?,
    interactionSource: MutableInteractionSource,
): Modifier = pointerInput(Unit, Unit) {
    detectTapGestures(
        onPress = {
            setTouchPoint(it.point)
            awaitRelease()
            setTouchPoint(null)
        }
    )
}
    .then(onZoom?.let(Modifier::zoomable) ?: Modifier)
    .then(
        scrollableState?.let { state ->
            scrollable(
                state = state,
                orientation = Orientation.Horizontal,
                interactionSource = interactionSource,
            )
        } ?: pointerInput(Unit, Unit) {
            detectDragGestures(
                onDragEnd = { setTouchPoint(null) },
                onDragCancel = { setTouchPoint(null) },
                onDrag = { change, _ -> setTouchPoint(change.position.point) }
            )
        }
    )

private val Offset.point: Point
    get() = Point(x, y)

/**
 * Adds the provided modifier elements to this modifier chain if [condition] is true.
 */
public inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier,
): Modifier = if (condition) factory() else this
