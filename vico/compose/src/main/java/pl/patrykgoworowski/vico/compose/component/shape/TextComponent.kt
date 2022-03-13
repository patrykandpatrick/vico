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

package pl.patrykgoworowski.vico.compose.component.shape

import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import pl.patrykgoworowski.vico.compose.extension.pixelSize
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.component.text.buildTextComponent
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.dimensions.emptyDimensions

/**
 * Creates a [TextComponent].
 * @param color the text color.
 * @param textSize the text size.
 * @param background an optional [ShapeComponent] to display behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param padding the padding between the text and the background.
 * @param margins the margins around the background.
 */
@Composable
public fun textComponent(
    color: Color = currentChartStyle.axis.axisLabelColor,
    textSize: TextUnit = currentChartStyle.axis.axisLabelTextSize,
    background: ShapeComponent? = currentChartStyle.axis.axisLabelBackground,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = currentChartStyle.axis.axisLabelLineCount,
    padding: MutableDimensions = emptyDimensions(),
    margins: MutableDimensions = emptyDimensions(),
): TextComponent = buildTextComponent {
    this.color = color.toArgb()
    this.textSizeSp = textSize.pixelSize()
    this.ellipsize = ellipsize
    this.lineCount = lineCount
    this.background = background
    this.padding = padding
    this.margins = margins
}
