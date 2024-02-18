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

package com.patrykandpatrick.vico.compose.common.component

import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.core.common.component.CartesianMarkerComponent
import com.patrykandpatrick.vico.core.common.component.Component
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent

/**
 * Creates a [CartesianMarkerComponent].
 *
 * @param label the [TextComponent] to use for the label.
 * @param labelPosition the [CartesianMarkerComponent.LabelPosition] to set the label position inside the chart
 * @param indicator the [Component] to use for the indicator.
 * @param guideline the [LineComponent] to use for the guideline.
 */
@Composable
public fun markerComponent(
    label: TextComponent,
    labelPosition: CartesianMarkerComponent.LabelPosition = CartesianMarkerComponent.LabelPosition.top(),
    indicator: com.patrykandpatrick.vico.core.common.component.Component,
    guideline: LineComponent,
): CartesianMarkerComponent =
    CartesianMarkerComponent(
        label = label,
        labelPosition = labelPosition,
        indicator = indicator,
        guideline = guideline,
    )
