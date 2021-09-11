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

package pl.patrykgoworowski.vico.compose.component

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import pl.patrykgoworowski.vico.compose.extension.pixelSize
import pl.patrykgoworowski.vico.core.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.path.Shape

@Composable
fun textComponent(
    color: Color,
    textSize: TextUnit,
    background: ShapeComponent<Shape>?,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = DEF_LABEL_LINE_COUNT,
): TextComponent = TextComponent(
    color = color.toArgb(),
    textSize = textSize.pixelSize(LocalDensity.current),
    ellipsize = ellipsize,
    lineCount = lineCount,
    background = background,
)
