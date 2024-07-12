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

package com.patrykandpatrick.vico.views.common

import android.content.Context
import android.content.res.Configuration
import android.view.View.LAYOUT_DIRECTION_LTR
import com.patrykandpatrick.vico.core.common.DefaultColors

internal val Context.density: Float
  get() = resources.displayMetrics.density

internal val Context.isLtr: Boolean
  get() = resources.configuration.layoutDirection == LAYOUT_DIRECTION_LTR

internal val Context.isDarkMode: Boolean
  get() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
      Configuration.UI_MODE_NIGHT_YES

internal val Context.defaultColors: DefaultColors
  get() = if (isDarkMode) DefaultColors.Dark else DefaultColors.Light
