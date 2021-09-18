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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.vico.app.ui.theme.flickrPink
import pl.patrykgoworowski.vico.compose.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.compose.axis.vertical.startAxis
import pl.patrykgoworowski.vico.compose.component.dimension.dimensionsOf
import pl.patrykgoworowski.vico.compose.component.shape.shader.componentShader
import pl.patrykgoworowski.vico.compose.component.shapeComponent
import pl.patrykgoworowski.vico.compose.dataset.bar.DataSet
import pl.patrykgoworowski.vico.compose.dataset.bar.lineDataSet
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryList
import pl.patrykgoworowski.vico.core.shape.Shapes.pillShape

@Composable
fun LineChart(
    modifier: Modifier = Modifier,
    entryList: EntryList,
) {
    DataSet(
        modifier = modifier,
        dataSet = lineDataSet(
            pointSize = 10.dp,
            lineColor = flickrPink,
            lineBackgroundShader = componentShader(
                component = shapeComponent(
                    shape = pillShape,
                    color = flickrPink,
                    margins = dimensionsOf(all = 0.5f.dp)
                ),
                componentSize = 4.dp,
            ),
        ),
        entryCollection = entryList,
        marker = markerComponent(),
        startAxis = startAxis(),
        bottomAxis = bottomAxis(),
    )
}
