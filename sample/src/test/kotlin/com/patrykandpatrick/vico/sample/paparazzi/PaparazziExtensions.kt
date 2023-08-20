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

package com.patrykandpatrick.vico.sample.paparazzi

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.NightMode

internal val lightConfig = DeviceConfig(
    screenWidth = 1080,
    screenHeight = 800,
)

internal val nightConfig = lightConfig.copy(
    nightMode = NightMode.NIGHT,
)

fun Paparazzi.gif(
    name: String? = null,
    start: Long = 0L,
    end: Long = 500L,
    fps: Int = 60,
    composable: @Composable () -> Unit,
) {
    val hostView = ComposeView(context)
    hostView.setContent(composable)
    gif(hostView, name, start, end, fps)
}
