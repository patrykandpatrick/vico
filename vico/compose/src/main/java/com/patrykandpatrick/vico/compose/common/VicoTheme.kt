/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.compose.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.DefaultColors

/**
 * Houses default chart colors.
 *
 * @param cartesianLayerColors used for [ColumnCartesianLayer]&#0020;[LineComponent]s and
 * [LineCartesianLayer.LineSpec]s.
 * @param elevationOverlayColor used for elevation overlays.
 * @param lineColor used for [HorizontalAxis] and [VerticalAxis] lines.
 * @param textColor used for [HorizontalAxis] and [VerticalAxis] labels.
 */
public data class VicoTheme(
    val cartesianLayerColors: List<Color>,
    val elevationOverlayColor: Color,
    val lineColor: Color,
    val textColor: Color,
) {
    internal companion object {
        fun fromDefaultColors(defaultColors: DefaultColors) =
            VicoTheme(
                defaultColors.cartesianLayerColors.map(::Color),
                Color(defaultColors.elevationOverlayColor),
                Color(defaultColors.lineColor),
                Color(defaultColors.textColor),
            )
    }
}

private val LocalVicoTheme = staticCompositionLocalOf<VicoTheme?> { null }

/** The current [VicoTheme]. */
public val vicoTheme: VicoTheme
    @Composable
    get() =
        LocalVicoTheme.current
            ?: run {
                val isSystemInDarkTheme = isSystemInDarkTheme()
                remember(isSystemInDarkTheme) {
                    VicoTheme.fromDefaultColors(if (isSystemInDarkTheme) DefaultColors.Dark else DefaultColors.Light)
                }
            }

/** Provides a [VicoTheme]. */
@Composable
public fun ProvideVicoTheme(
    theme: VicoTheme,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalVicoTheme provides theme, content)
}
