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

package com.patrykandpatrick.vico.sample.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun VicoTheme(content: @Composable () -> Unit) {
    val darkColorScheme = darkColorScheme(
        surface = Color(color = DARK_SURFACE),
        background = Color.Black,
    )
    val lightColorScheme = lightColorScheme(
        surface = Color.White,
        background = Color(color = LIGHT_BACKGROUND),
    )
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme else lightColorScheme,
        typography = Typography(),
        content = content,
    )
}

private const val LIGHT_BACKGROUND = 0xFFF5F5F7
private const val DARK_SURFACE = 0xFF1C1E21
