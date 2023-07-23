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

package com.patrykandpatrick.vico.compose.gesture

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo
import com.patrykandpatrick.vico.compose.extension.detectZoomGestures

/**
 * Represents a function block that handles the pinch-to-zoom gesture.
 * `centroid` is the average position of the touch points.
 * `zoomChange` is the factor by which the content scale should be multiplied.
 */
public typealias OnZoom = (centroid: Offset, zoomChange: Float) -> Unit

/**
 * Handles the pinch-to-zoom gesture.
 *
 * @param onZoom called when a pinch gesture is detected.
 * @param enabled whether zooming is enabled.
 */
public fun Modifier.zoomable(
    onZoom: OnZoom,
    enabled: Boolean = true,
): Modifier = composed(
    factory = {
        val block: suspend PointerInputScope.() -> Unit = remember {
            {
                detectZoomGestures { centroid, zoom ->
                    onZoom(centroid, zoom)
                }
            }
        }
        if (enabled) Modifier.pointerInput(Unit, block) else Modifier
    },
    inspectorInfo = debugInspectorInfo {
        name = "zoomable"
        properties["enabled"] = enabled
    },
)
