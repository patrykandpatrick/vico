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

package com.patrykandpatrick.vico.compose.m2.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.style.ChartStyle

/**
 * Creates a baseline [ChartStyle] implementation using the colors provided via [MaterialTheme.colors].
 * The colors may be customized.
 */
@Composable
public fun m2ChartStyle(
    axisLabelColor: Color = MaterialTheme.colors.onBackground,
    axisGuidelineColor: Color = MaterialTheme.colors.onBackground.copy(alpha = LINE_ALPHA),
    axisLineColor: Color = MaterialTheme.colors.onBackground.copy(alpha = LINE_ALPHA),
    entityColors: List<Color> = listOf(
        MaterialTheme.colors.primary,
        MaterialTheme.colors.secondary,
    ),
    elevationOverlayColor: Color = if (isSystemInDarkTheme()) MaterialTheme.colors.onBackground else Color.Transparent,
): ChartStyle = remember(
    axisLabelColor,
    axisGuidelineColor,
    axisLineColor,
    entityColors,
    elevationOverlayColor,
) {
    ChartStyle.fromColors(
        axisLabelColor = axisLabelColor,
        axisGuidelineColor = axisGuidelineColor,
        axisLineColor = axisLineColor,
        entityColors = entityColors,
        elevationOverlayColor = elevationOverlayColor,
    )
}

private const val LINE_ALPHA = 0.2f
