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

package pl.patrykgoworowski.vico.compose.dataset.bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.compose.extension.pixels
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.vico.core.constants.DEF_MERGED_BAR_SPACING
import pl.patrykgoworowski.vico.core.dataset.bar.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.bar.MergeMode

@Composable
fun columnDataSet(
    columns: List<LineComponent>,
    spacing: Dp = DEF_MERGED_BAR_SPACING.dp,
    innerSpacing: Dp = DEF_MERGED_BAR_INNER_SPACING.dp,
    mergeMode: MergeMode = MergeMode.Grouped,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnDataSet {
    val dataSet = remember { ColumnDataSet() }
    return dataSet.apply {
        this.columns = columns
        this.spacing = spacing.pixels
        this.innerSpacing = innerSpacing.pixels
        this.mergeMode = mergeMode
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
    }
}

@Composable
fun columnDataSet(
    column: LineComponent,
    spacing: Dp = DEF_MERGED_BAR_SPACING.dp,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): ColumnDataSet = columnDataSet(
    columns = listOf(column),
    spacing = spacing,
    minX = minX,
    maxX = maxX,
    minY = minY,
    maxY = maxY,
)