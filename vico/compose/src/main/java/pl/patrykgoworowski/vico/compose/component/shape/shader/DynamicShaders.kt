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

package pl.patrykgoworowski.vico.compose.component.shape.shader

import android.graphics.Shader
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.core.component.Component
import pl.patrykgoworowski.vico.core.component.shape.shader.ComponentShader
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders

public fun DynamicShaders.fromComponent(
    component: Component,
    componentSize: Dp,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
): ComponentShader = ComponentShader(
    component = component,
    componentSizeDp = componentSize.value,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)

/**
 * Creates a [BrushShader] using the given [Brush].
 *
 * @see BrushShader
 */
public fun DynamicShaders.fromBrush(brush: Brush): BrushShader = BrushShader(brush)
