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

package com.patrykandpatrick.vico.sample.extension

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.style.ChartStyle
import com.patrykandpatrick.vico.core.DefaultColors

@Composable
internal fun ChartStyle.Companion.fromEntityColors(entityColors: LongArray): ChartStyle {
    val defaultColors = if (isSystemInDarkTheme()) DefaultColors.Dark else DefaultColors.Light
    return fromColors(
        axisLabelColor = Color(defaultColors.axisLabelColor),
        axisGuidelineColor = Color(defaultColors.axisGuidelineColor),
        axisLineColor = Color(defaultColors.axisLineColor),
        entityColors = entityColors.map(::Color),
        elevationOverlayColor = Color(defaultColors.elevationOverlayColor),
    )
}
