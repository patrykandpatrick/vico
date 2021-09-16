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
import pl.patrykgoworowski.vico.app.ui.theme.purple
import pl.patrykgoworowski.vico.app.ui.theme.trypanPurple
import pl.patrykgoworowski.vico.compose.component.rectComponent
import pl.patrykgoworowski.vico.compose.dataset.bar.DataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.columnDataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.lineDataSet
import pl.patrykgoworowski.vico.core.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.core.axis.vertical.startAxis
import pl.patrykgoworowski.vico.core.dataset.composed.plus
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.composed.ComposedEntryCollection
import pl.patrykgoworowski.vico.core.shape.Shapes.pillShape

@Composable
fun ComposedChart(
    modifier: Modifier = Modifier,
    model: ComposedEntryCollection<EntryModel>,
) {
    DataSet(
        modifier = modifier,
        dataSet = columnDataSet(
            columns = listOf(
                firstRectComponent(),
                secondRectComponent(),
                thirdRectComponent()
            ),
        ) + lineDataSet(
            lineColor = flickrPink,
            spacing = 8.dp,
        ),
        entryCollection = model,
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
        marker = markerComponent(),
    )
}

@Composable
fun firstRectComponent() = rectComponent(
    color = trypanPurple,
    thickness = 16.dp,
    shape = CutCornerShape(topStart = 8.dp),
)

@Composable
fun secondRectComponent() = rectComponent(
    color = byzantine,
    thickness = 12.dp,
    shape = pillShape,
)

@Composable
fun thirdRectComponent() = rectComponent(
    color = purple,
    thickness = 16.dp,
    shape = CutCornerShape(topEnd = 8.dp),
)
