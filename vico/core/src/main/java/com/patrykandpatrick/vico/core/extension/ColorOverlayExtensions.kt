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

package com.patrykandpatrick.vico.core.extension

import android.graphics.Color
import com.patrykandpatrick.vico.core.context.DrawContext

private fun getElevationOverlayColorWithCorrectAlpha(
    elevationOverlayColor: Long,
    elevationDp: Float,
): Int {
    val overlayPercentage =
        when {
            elevationDp < 1f -> 0.00f
            elevationDp < 2f -> 0.05f
            elevationDp < 3f -> 0.07f
            elevationDp < 4f -> 0.08f
            elevationDp < 6f -> 0.09f
            elevationDp < 8f -> 0.11f
            elevationDp < 12f -> 0.12f
            elevationDp < 16f -> 0.14f
            elevationDp < 24f -> 0.15f
            else -> 0.16f
        }
    return if (elevationOverlayColor.alpha == 0f) {
        Color.TRANSPARENT
    } else {
        elevationOverlayColor.toInt().copyColor(alpha = overlayPercentage)
    }
}

/**
 * Overlays the given [color] with [DrawContext.elevationOverlayColor], changing the opacity of
 * [DrawContext.elevationOverlayColor] depending on the value of [elevationDp].
 */
public fun DrawContext.applyElevationOverlayToColor(
    color: Int,
    elevationDp: Float,
): Int =
    color.overlayColor(
        getElevationOverlayColorWithCorrectAlpha(
            elevationOverlayColor = elevationOverlayColor,
            elevationDp = elevationDp,
        ),
    )
