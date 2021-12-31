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

package pl.patrykgoworowski.vico.compose.style

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import pl.patrykgoworowski.vico.core.Colors

public object LocalChartColors {

    private val LocalLightColors: ProvidableCompositionLocal<Colors> =
        compositionLocalOf { Colors.Light }

    private val LocalDarkColors: ProvidableCompositionLocal<Colors> =
        compositionLocalOf { Colors.Dark }

    private val LocalProvidedStyle: ProvidableCompositionLocal<Colors?> =
        compositionLocalOf { null }

    public val current: Colors
        @Composable get() = LocalProvidedStyle.current
            ?: if (isSystemInDarkTheme()) LocalDarkColors.current else LocalLightColors.current

    public infix fun provides(colors: Colors): ProvidedValue<Colors?> =
        LocalProvidedStyle.provides(colors)
}

public val currentChartColors: Colors
    @Composable get() = LocalChartColors.current
