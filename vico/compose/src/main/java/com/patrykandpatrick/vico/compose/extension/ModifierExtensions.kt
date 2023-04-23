/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.patrykandpatrick.vico.compose.extension

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import com.patrykandpatrick.vico.compose.chart.scroll.ChartScrollState
import com.patrykandpatrick.vico.compose.gesture.OnZoom
import com.patrykandpatrick.vico.core.model.Point
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

internal fun Modifier.chartTouchEvent(
    setTouchPoint: ((Point?) -> Unit)?,
    isScrollEnabled: Boolean,
    scrollableState: ChartScrollState,
    onZoom: OnZoom?,
    interactionSource: MutableInteractionSource,
): Modifier =
    scrollable(
        state = scrollableState,
        orientation = Orientation.Horizontal,
        interactionSource = interactionSource,
        reverseDirection = true,
        enabled = isScrollEnabled,
    ).pointerInput(scrollableState, setTouchPoint, onZoom) {
        coroutineScope {
            if (setTouchPoint != null) {
                launch {
                    detectTapGestures(
                        onPress = {
                            setTouchPoint(it.point)
                            awaitRelease()
                            setTouchPoint(null)
                        },
                    )
                }
            }

            if (isScrollEnabled.not() && setTouchPoint != null) {
                launch {
                    detectHorizontalDragGestures(
                        onDragCancel = {
                            setTouchPoint(null)
                        },
                        onDragEnd = {
                            setTouchPoint(null)
                        },
                        onDragStart = { offset ->
                            setTouchPoint(offset.point)
                        },
                    ) { change, _ ->
                        setTouchPoint(change.position.point)
                    }
                }
            }

            if (onZoom != null && isScrollEnabled) {
                launch {
                    detectZoomGestures() { centroid, zoom ->
                        setTouchPoint?.invoke(null)
                        onZoom(centroid, zoom)
                    }
                }
            }
        }
    }

private val Offset.point: Point
    get() = Point(x, y)

/**
 * Adds the provided modifier elements to this modifier chain if [condition] is true.
 */
public inline fun Modifier.addIf(
    condition: Boolean,
    crossinline factory: Modifier.() -> Modifier,
): Modifier = if (condition) factory() else this

/**
 * Adds the provided modifier elements to this modifier chain if [value] is not null.
 */
public inline fun <T> Modifier.addIfNotNull(
    value: T?,
    crossinline factory: Modifier.(T) -> Modifier,
): Modifier = if (value != null) factory(value) else this
