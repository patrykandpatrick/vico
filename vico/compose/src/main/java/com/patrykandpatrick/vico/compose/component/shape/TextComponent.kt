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

package com.patrykandpatrick.vico.compose.component.shape

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.core.DEF_LABEL_LINE_COUNT
import com.patrykandpatrick.vico.core.DefaultDimens
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.dimensions.emptyDimensions

/**
 * Creates a [TextComponent].
 *
 * @param color the text color.
 * @param textSize the text size.
 * @param background an optional [ShapeComponent] to display behind the text.
 * @param ellipsize the text truncation behavior.
 * @param lineCount the line count.
 * @param padding the padding between the text and the background.
 * @param margins the margins around the background.
 * @param typeface the [Typeface] for the text.
 * @param textAlign the text alignment.
 */
@Composable
@Deprecated(message = "Use `com.patrykandpatrick.vico.compose.component.textComponent` instead.")
public fun textComponent(
    color: Color = Color.Black,
    textSize: TextUnit = DefaultDimens.TEXT_COMPONENT_TEXT_SIZE.sp,
    background: ShapeComponent? = null,
    ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    lineCount: Int = DEF_LABEL_LINE_COUNT,
    padding: MutableDimensions = emptyDimensions(),
    margins: MutableDimensions = emptyDimensions(),
    typeface: Typeface? = null,
    textAlign: Paint.Align = Paint.Align.LEFT,
): TextComponent = com.patrykandpatrick.vico.compose.component.textComponent(
    color = color,
    textSize = textSize,
    background = background,
    ellipsize = ellipsize,
    lineCount = lineCount,
    padding = padding,
    margins = margins,
    typeface = typeface,
    textAlign = textAlign,
)
