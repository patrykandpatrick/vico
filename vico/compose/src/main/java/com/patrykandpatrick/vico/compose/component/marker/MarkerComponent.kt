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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.component.Component
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.text.TextComponent
import com.patrykandpatrick.vico.core.marker.DefaultMarkerLabelFormatter
import com.patrykandpatrick.vico.core.marker.MarkerLabelFormatter

/** Creates and remembers a [MarkerComponent]. */
@Composable
public fun rememberMarkerComponent(
    label: TextComponent,
    labelFormatter: MarkerLabelFormatter = remember { DefaultMarkerLabelFormatter() },
    labelPosition: MarkerComponent.LabelPosition = MarkerComponent.LabelPosition.Top,
    indicator: Component? = null,
    guideline: LineComponent? = null,
): MarkerComponent =
    remember(label, labelPosition, indicator, guideline) { MarkerComponent(label, labelPosition, indicator, guideline) }
        .apply { this.labelFormatter = labelFormatter }

/** Creates and remembers a [MarkerComponent]. */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(
    "Use the overload with a `labelFormatter` parameter instead. (If you’re using named arguments, ignore this " +
        "warning. The deprecated overload is more specific, but the new one matches and will be used once the " +
        "deprecated one has been removed.)",
)
@Composable
public fun rememberMarkerComponent(
    label: TextComponent,
    labelPosition: MarkerComponent.LabelPosition = MarkerComponent.LabelPosition.Top,
    indicator: Component? = null,
    guideline: LineComponent? = null,
): MarkerComponent =
    remember(label, labelPosition, indicator, guideline) { MarkerComponent(label, labelPosition, indicator, guideline) }

/**
 * Creates a [MarkerComponent].
 */
@Composable
@Deprecated(
    "Use `rememberMarkerComponent` instead.",
    ReplaceWith(
        "rememberMarkerComponent(label = label, indicator = indicator, guideline = guideline)",
        "com.patrykandpatrick.vico.compose.component.marker.rememberMarkerComponent",
    ),
)
public fun markerComponent(
    label: TextComponent,
    indicator: Component,
    guideline: LineComponent,
): MarkerComponent =
    @Suppress("DEPRECATION")
    rememberMarkerComponent(label = label, indicator = indicator, guideline = guideline)
