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
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.compose.extension.pixels
import pl.patrykgoworowski.vico.core.component.Component

@Composable
fun componentShader(
    component: Component,
    componentSize: Dp,
    checkeredArrangement: Boolean = true,
    tileXMode: Shader.TileMode = Shader.TileMode.REPEAT,
    tileYMode: Shader.TileMode = tileXMode,
) = pl.patrykgoworowski.vico.core.component.shape.shader.componentShader(
    component = component,
    componentSize = componentSize.pixels,
    checkeredArrangement = checkeredArrangement,
    tileXMode = tileXMode,
    tileYMode = tileYMode,
)
