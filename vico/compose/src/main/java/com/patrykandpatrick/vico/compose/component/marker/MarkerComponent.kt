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

package com.patrykandpatrick.vico.compose.component.marker

import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.marker.Marker

/**
 * Creates a [MarkerComponent].
 *
 * @param label the [TextComponent] to use for the label.
 * @param labelPosition the [MarkerComponent.LabelPosition] to set the label position inside the chart
 * @param indicator the [Component] to use for the indicator.
 * @param guideline the [LineComponent] to use for the guideline.
 */
@Composable
public fun markerComponent(
    label: TextComponent,
    labelPosition: MarkerComponent.LabelPosition,
    indicator: Component,
    guideline: LineComponent,
): MarkerComponent =
    MarkerComponent(
        label = label,
        labelPosition = labelPosition,
        indicator = indicator,
        guideline = guideline,
    )
