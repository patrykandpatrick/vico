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

package pl.patrykgoworowski.vico.app.showcase

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.component.view.getMarkerComponent
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
import pl.patrykgoworowski.vico.core.dataset.bar.ColumnDataSet
import pl.patrykgoworowski.vico.core.dataset.bar.MergeMode
import pl.patrykgoworowski.vico.core.dataset.composed.ComposedDataSet
import pl.patrykgoworowski.vico.core.dataset.composed.composedEntryModel
import pl.patrykgoworowski.vico.core.dataset.entry.collection.collectAsFlow
import pl.patrykgoworowski.vico.core.dataset.entry.collection.entryModel
import pl.patrykgoworowski.vico.core.dataset.line.LineDataSet
import pl.patrykgoworowski.vico.core.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.extension.dp
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.path.Shapes.cutCornerShape
import pl.patrykgoworowski.vico.core.path.Shapes.pillShape
import pl.patrykgoworowski.vico.databinding.FragmentViewBinding
import pl.patrykgoworowski.vico.view.dataset.DataSetView
import pl.patrykgoworowski.vico.view.dataset.common.plus

class ViewShowcaseFragment : Fragment(R.layout.fragment_view) {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentViewBinding.bind(view)

        val marker = getMarkerComponent(view.context)

        setUpBar(binding.barChart, marker)
        setUpLineChart(binding.lineChart, marker)
        setUpComposedChart(binding.composedChart, marker)
        setUpGroupedBar(binding.groupedColumnChart, marker)
        setUpStackedBar(binding.stackedColumnChart, marker)
    }

    private fun setUpBar(dataSetView: DataSetView, marker: Marker?) {
        val context = dataSetView.context

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
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpComposedChart(dataSetView: DataSetView, marker: Marker?) {
        val context = dataSetView.context

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
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpLineChart(dataSetView: DataSetView, marker: Marker?) {
        val context = dataSetView.context

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
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpGroupedBar(dataSetView: DataSetView, marker: Marker?) {
        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = requireContext().flickrPink,
                    thickness = 16f.dp,
                    shape = cutCornerShape(topLeft = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().byzantine,
                    thickness = 24f.dp,
                ),
                LineComponent(
                    color = requireContext().trypanPurple,
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
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpStackedBar(dataSetView: DataSetView, marker: Marker?) {
        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = requireContext().color { R.color.flickr_pink },
                    thickness = 16f.dp,
                    shape = cutCornerShape(bottomRight = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().color { R.color.byzantine },
                    thickness = 16f.dp,
                ),
                LineComponent(
                    color = requireContext().color { R.color.trypan_purple },
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
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }
}
