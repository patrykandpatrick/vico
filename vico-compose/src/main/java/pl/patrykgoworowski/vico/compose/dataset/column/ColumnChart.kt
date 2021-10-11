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

package pl.patrykgoworowski.vico.compose.dataset.column

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.column.MergeMode

@Composable
fun columnDataSet(
    columns: List<LineComponent> = currentChartStyle.columnChart.columns,
    spacing: Dp = currentChartStyle.columnChart.outsideSpacing,
    innerSpacing: Dp = currentChartStyle.columnChart.innerSpacing,
    mergeMode: MergeMode = MergeMode.Grouped,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnDataSet {
    val dataSet = remember { ColumnDataSet() }
    return dataSet.apply {
        this.columns = columns
        this.spacingDp = spacing.value
        this.innerSpacingDp = innerSpacing.value
        this.mergeMode = mergeMode
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
    }
}
