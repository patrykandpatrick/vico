/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.sample.component

import android.graphics.Typeface
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.text.textComponent
import com.patrykandpatryk.vico.view.dimensions.dimensionsOf

internal fun getThresholdLineLabel(
    color: Int,
    backgroundColor: Int,
    strokeColor: Int,
) = textComponent {
    this.color = color
    typeface = Typeface.MONOSPACE
    margins = dimensionsOf(allDp = MARGIN_DP)
    padding = dimensionsOf(
        verticalDp = VERTICAL_PADDING_DP,
        horizontalDp = HORIZONTAL_PADDING_DP,
    )
    background = ShapeComponent(
        strokeColor = strokeColor,
        color = backgroundColor,
        shape = Shapes.pillShape,
        strokeWidthDp = STROKE_WIDTH_DP,
    )
}

private const val MARGIN_DP = 4f
private const val HORIZONTAL_PADDING_DP = 8f
private const val VERTICAL_PADDING_DP = 4f
private const val STROKE_WIDTH_DP = 2f
