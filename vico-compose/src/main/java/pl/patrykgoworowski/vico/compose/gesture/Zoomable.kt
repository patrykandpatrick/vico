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

package pl.patrykgoworowski.vico.compose.gesture

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo

public typealias OnZoom = (centroid: Offset, zoomChange: Float) -> Unit

@SuppressLint("UnnecessaryComposedModifier")
public fun Modifier.zoomable(
    onZoom: OnZoom,
    enabled: Boolean = true,
): Modifier = composed(
    factory = {
        val block: suspend PointerInputScope.() -> Unit = remember {
            {
                forEachGesture {
                    detectTransformGestures { centroid, _, zoom, _ ->
                        onZoom(centroid, zoom)
                    }
                }
            }
        }
        if (enabled) Modifier.pointerInput(Unit, block) else Modifier
    },
    inspectorInfo = debugInspectorInfo {
        name = "zoomable"
        properties["enabled"] = enabled
    }
)
