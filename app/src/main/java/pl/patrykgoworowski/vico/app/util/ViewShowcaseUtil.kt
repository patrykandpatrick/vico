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
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.shader.DynamicShaders
import pl.patrykgoworowski.vico.core.dataset.column.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.column.MergeMode
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.composedEntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.collectAsFlow
import pl.patrykgoworowski.vico.core.dataset.entry.collection.entryModel
import pl.patrykgoworowski.vico.core.dataset.line.LineDataSet
import pl.patrykgoworowski.vico.core.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.component.shape.Shapes.pillShape
import pl.patrykgoworowski.vico.view.component.shape.cutCornerShape
import pl.patrykgoworowski.vico.view.component.shape.shader.fromComponent
import pl.patrykgoworowski.vico.view.component.shape.shader.horizontalGradient
import pl.patrykgoworowski.vico.view.component.shape.shader.verticalGradient
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
                shape = Shapes.cutCornerShape(topLeft = 8f),
                thicknessDp = 16f,
                dynamicShader = DynamicShaders
                    .horizontalGradient(context.flickrPink, context.trypanPurple),
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
                            thicknessDp = 16f,
                            shape = Shapes.cutCornerShape(topLeft = 8f),
                        ),
                        LineComponent(
                            color = context.byzantine,
                            thicknessDp = 12f,
                            shape = pillShape,
                        ),
                        LineComponent(
                            color = context.purple,
                            thicknessDp = 16f,
                            shape = Shapes.cutCornerShape(topRight = 8f),
                        ),
                    ),
                ), LineDataSet(
                    pointSizeDp = 62f,
                    lineColor = context.flickrPink,
                    lineWidthDp = 2f,
                    spacingDp = 8f,
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
            pointSizeDp = 10f,
            lineColor = context.flickrPink
        ).apply {
            isHorizontalScrollEnabled = true
            lineBackgroundShader = DynamicShaders
                .verticalGradient(context.flickrPink.copyColor(alpha = 128), Color.TRANSPARENT,)
            lineBackgroundShader = DynamicShaders.fromComponent(
                component = ShapeComponent(
                    shape = pillShape,
                    color = context.flickrPink,
                    margins = dimensionsOf(allDp = .5f)
                ),
                componentSize = 4f,
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
                    thicknessDp = 16f,
                    shape = Shapes.cutCornerShape(topLeft = 8f)
                ),
                LineComponent(
                    color = context.byzantine,
                    thicknessDp = 24f,
                ),
                LineComponent(
                    color = context.trypanPurple,
                    thicknessDp = 16f,
                    shape = Shapes.cutCornerShape(topRight = 8f)
                ),
            ),
            mergeMode = MergeMode.Grouped,
            spacingDp = 24f,
            innerSpacingDp = 4f,
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
                    thicknessDp = 16f,
                    shape = Shapes.cutCornerShape(bottomRight = 8f)
                ),
                LineComponent(
                    color = context.color { R.color.byzantine },
                    thicknessDp = 16f,
                ),
                LineComponent(
                    color = context.color { R.color.trypan_purple },
                    thicknessDp = 16f,
                    shape = Shapes.cutCornerShape(topLeft = 8f)
                ),
            ),
            mergeMode = MergeMode.Stack,
            spacingDp = 24f,
            innerSpacingDp = 4f,
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
