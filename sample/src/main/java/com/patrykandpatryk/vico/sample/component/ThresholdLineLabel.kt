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
import androidx.compose.ui.unit.dp
import com.patrykandpatryk.vico.compose.component.shape.roundedCornerShape
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.component.text.buildTextComponent
import com.patrykandpatryk.vico.core.dimensions.MutableDimensions

internal fun getThresholdLineLabel(
    color: Int,
    backgroundColor: Int,
) = buildTextComponent {
    this.color = color
    typeface = Typeface.MONOSPACE
    margins = MutableDimensions(
        startDp = START_MARGIN_DP,
        topDp = 0f,
        endDp = 0f,
        bottomDp = 0f,
    )
    padding = MutableDimensions(
        startDp = HORIZONTAL_PADDING_DP,
        topDp = 0f,
        endDp = HORIZONTAL_PADDING_DP,
        bottomDp = 0f,
    )
    background = ShapeComponent(
        color = backgroundColor,
        shape = Shapes.roundedCornerShape(
            topLeft = BACKGROUND_TOP_CORNER_RADIUS_DP.dp,
            topRight = BACKGROUND_TOP_CORNER_RADIUS_DP.dp,
        ),
    )
}

private const val START_MARGIN_DP = 4f
private const val HORIZONTAL_PADDING_DP = 8f
private const val BACKGROUND_TOP_CORNER_RADIUS_DP = 4
