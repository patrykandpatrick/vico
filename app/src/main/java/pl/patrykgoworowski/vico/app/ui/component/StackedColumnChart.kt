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

package pl.patrykgoworowski.vico.app.ui.component

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.app.ui.theme.byzantine
import pl.patrykgoworowski.vico.app.ui.theme.flickrPink
import pl.patrykgoworowski.vico.app.ui.theme.trypanPurple
import pl.patrykgoworowski.vico.compose.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.compose.axis.vertical.startAxis
import pl.patrykgoworowski.vico.compose.component.rectComponent
import pl.patrykgoworowski.vico.compose.dataset.bar.DataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.columnDataSet
import pl.patrykgoworowski.vico.core.dataset.column.MergeMode
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryList

@Composable
fun StackedColumnChart(
    modifier: Modifier = Modifier,
    entryList: EntryList,
) {
    DataSet(
        modifier = modifier,
        dataSet = columnDataSet(
            columns = listOf(
                rectComponent(
                    color = flickrPink,
                    shape = CutCornerShape(bottomEnd = 8.dp)
                ),
                rectComponent(color = byzantine),
                rectComponent(
                    color = trypanPurple,
                    shape = CutCornerShape(topStart = 8.dp)
                ),
            ),
            innerSpacing = 4.dp,
            spacing = 24.dp,
            mergeMode = MergeMode.Stack,
        ),
        entryCollection = entryList,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        marker = markerComponent(),
    )
}
