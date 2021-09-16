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

package pl.patrykgoworowski.vico.app.util

import android.content.Context
import android.graphics.Color
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.ShowcaseViewModel
import pl.patrykgoworowski.vico.app.extension.byzantine
import pl.patrykgoworowski.vico.app.extension.color
import pl.patrykgoworowski.vico.app.extension.flickrPink
import pl.patrykgoworowski.vico.app.extension.purple
import pl.patrykgoworowski.vico.app.extension.trypanPurple
import pl.patrykgoworowski.vico.core.axis.horizontal.bottomAxis
import pl.patrykgoworowski.vico.core.axis.vertical.startAxis
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.shader.componentShader
import pl.patrykgoworowski.vico.core.component.shape.shader.horizontalGradient
import pl.patrykgoworowski.vico.core.component.shape.shader.verticalGradient
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.column.MergeMode
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.composedEntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.collectAsFlow
import pl.patrykgoworowski.vico.core.dataset.entry.collection.entryModel
import pl.patrykgoworowski.vico.core.dataset.line.LineDataSet
import pl.patrykgoworowski.vico.core.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.extension.dp
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.shape.Shapes.cutCornerShape
import pl.patrykgoworowski.vico.core.shape.Shapes.pillShape
import pl.patrykgoworowski.vico.view.dataset.DataSetView
import pl.patrykgoworowski.vico.view.dataset.common.plus

@ExperimentalCoroutinesApi
class ViewShowcaseUtil(
    private val viewModel: ShowcaseViewModel,
    private val context: Context,
    private val coroutineScope: CoroutineScope
) {

    fun setUpColumnChart(dataSetView: DataSetView, marker: Marker?) {

        val columnDataSet = ColumnDataSet(
            column = LineComponent(
                color = context.flickrPink,
                shape = cutCornerShape(topLeft = 8f.dp),
                thickness = 16f.dp,
                dynamicShader = horizontalGradient(context.flickrPink, context.trypanPurple),
            ),
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + entryModel()

        viewModel.entries.collectAsFlow
            .onEach(dataSetRenderer::setModel)
            .launchIn(coroutineScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    fun setUpComposedChart(dataSetView: DataSetView, marker: Marker?) {

        val composedDataSet = ComposedDataSet(
            listOf(
                ColumnDataSet(
                    columns = listOf(
                        LineComponent(
                            color = context.trypanPurple,
                            thickness = 16.dp,
                            shape = cutCornerShape(topLeft = 8.dp),
                        ),
                        LineComponent(
                            color = context.byzantine,
                            thickness = 12.dp,
                            shape = pillShape,
                        ),
                        LineComponent(
                            color = context.purple,
                            thickness = 16.dp,
                            shape = cutCornerShape(topRight = 8.dp),
                        ),
                    ),
                ), LineDataSet(
                    pointSize = 62.dp,
                    lineColor = context.flickrPink,
                    spacing = 8.dp
                )
            )
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = composedDataSet + composedEntryModel()

        viewModel.composedEntries.collectAsFlow
            .onEach(dataSetRenderer::setModel)
            .launchIn(coroutineScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    fun setUpLineChart(dataSetView: DataSetView, marker: Marker?) {

        val lineDataSet = LineDataSet(
            pointSize = 10.dp,
            lineColor = context.flickrPink
        ).apply {
            isHorizontalScrollEnabled = true
            lineBackgroundShader = verticalGradient(
                context.flickrPink.copyColor(alpha = 128),
                Color.TRANSPARENT,
            )
            lineBackgroundShader = componentShader(
                component = ShapeComponent(
                    shape = pillShape,
                    color = context.flickrPink,
                    margins = dimensionsOf(all = 0.5f.dp)
                ),
                componentSize = 4.dp,
            )
        }

        val dataSetRenderer = lineDataSet + entryModel()

        viewModel.entries.collectAsFlow
            .onEach(dataSetRenderer::setModel)
            .launchIn(coroutineScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    fun setUpGroupedColumnChart(dataSetView: DataSetView, marker: Marker?) {

        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = context.flickrPink,
                    thickness = 16f.dp,
                    shape = cutCornerShape(topLeft = 8f.dp)
                ),
                LineComponent(
                    color = context.byzantine,
                    thickness = 24f.dp,
                ),
                LineComponent(
                    color = context.trypanPurple,
                    thickness = 16f.dp,
                    shape = cutCornerShape(topRight = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Grouped,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + entryModel()

        viewModel.multiEntries.collectAsFlow
            .onEach(dataSetRenderer::setModel)
            .launchIn(coroutineScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    fun setUpStackedColumnChart(dataSetView: DataSetView, marker: Marker?) {

        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = context.color { R.color.flickr_pink },
                    thickness = 16f.dp,
                    shape = cutCornerShape(bottomRight = 8f.dp)
                ),
                LineComponent(
                    color = context.color { R.color.byzantine },
                    thickness = 16f.dp,
                ),
                LineComponent(
                    color = context.color { R.color.trypan_purple },
                    thickness = 16f.dp,
                    shape = cutCornerShape(topLeft = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Stack,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + entryModel()

        viewModel.multiEntries.collectAsFlow
            .onEach(dataSetRenderer::setModel)
            .launchIn(coroutineScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }
}
