/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.component.text

import android.graphics.RectF
import com.patrykandpatrick.vico.core.extension.half

internal fun VerticalPosition.inBounds(
    bounds: RectF,
    distanceFromPoint: Float = 0f,
    componentHeight: Float,
    y: Float,
): VerticalPosition {
    val topFits = y - distanceFromPoint - componentHeight >= bounds.top
    val centerFits = y - componentHeight.half >= bounds.top && y + componentHeight.half <= bounds.bottom
    val bottomFits = y + distanceFromPoint + componentHeight <= bounds.bottom

    return when (this) {
        VerticalPosition.Top -> if (topFits) this else VerticalPosition.Bottom
        VerticalPosition.Bottom -> if (bottomFits) this else VerticalPosition.Top
        VerticalPosition.Center -> when {
            centerFits -> this
            topFits -> VerticalPosition.Top
            else -> VerticalPosition.Bottom
        }
    }
}
