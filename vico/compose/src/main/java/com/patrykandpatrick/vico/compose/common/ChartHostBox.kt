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

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.core.common.Defaults
import com.patrykandpatrick.vico.core.common.legend.Legend

@Composable
internal fun ChartHostBox(
    modifier: Modifier,
    legend: Legend?,
    hasModel: Boolean,
    desiredHeight: MeasureScope.(Constraints) -> Int,
    content: @Composable () -> Unit,
) {
    val preMeasureContext = rememberPreMeasureContext()
    Layout(content, modifier.heightIn(min = Defaults.CHART_HEIGHT.dp)) { measurable, constraints ->
        val chartMeasurable = measurable.first()
        val additionalHeight =
            if (hasModel && legend != null) {
                legend.getHeight(preMeasureContext, constraints.maxWidth.toFloat()).toInt()
            } else {
                0
            }
        val height =
            (constraints.minHeight.coerceAtLeast(desiredHeight(constraints)) + additionalHeight)
                .coerceAtMost(constraints.maxHeight)

        val chartConstraints =
            constraints.copy(
                minWidth = constraints.maxWidth,
                minHeight = height,
                maxHeight = height,
            )
        val placeable = chartMeasurable.measure(chartConstraints)
        layout(placeable.width, placeable.height) { placeable.place(0, 0) }
    }
}
