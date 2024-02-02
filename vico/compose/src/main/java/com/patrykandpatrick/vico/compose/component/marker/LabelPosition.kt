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

package com.patrykandpatrick.vico.compose.component.marker

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent.LabelPosition

/**
 * Returns the instance of the [LabelPosition.Top] object
 */
public fun LabelPosition.Companion.top(): LabelPosition.Top = LabelPosition.Top

/**
 * Indicates the label should be rendered above the marker indicator
 *
 * @param spacing space in [Dp] between the indicator and the label
 * @return an instance of the [LabelPosition.AboveIndicator]
 */
public fun LabelPosition.Companion.aboveIndicator(spacing: Dp = 2.dp): LabelPosition.AboveIndicator =
    LabelPosition.AboveIndicator(spacing.value)
