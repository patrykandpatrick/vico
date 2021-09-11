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

package pl.patrykgoworowski.liftchart.showcase

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.liftchart.R
import pl.patrykgoworowski.liftchart.component.view.getMarkerComponent
import pl.patrykgoworowski.liftchart.databinding.FragmentViewBinding
import pl.patrykgoworowski.liftchart.extension.*
import pl.patrykgoworowski.liftchart_common.axis.horizontal.bottomAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.startAxis
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.componentShader
import pl.patrykgoworowski.liftchart_common.component.shape.shader.horizontalGradient
import pl.patrykgoworowski.liftchart_common.component.shape.shader.verticalGradient
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSet
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.composed.ComposedDataSet
import pl.patrykgoworowski.liftchart_common.data_set.emptyComposedEntryModel
import pl.patrykgoworowski.liftchart_common.data_set.emptyEntryModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.collectAsFlow
import pl.patrykgoworowski.liftchart_common.data_set.line.LineDataSet
import pl.patrykgoworowski.liftchart_common.dimensions.dimensionsOf
import pl.patrykgoworowski.liftchart_common.extension.copyColor
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.path.CutCornerShape
import pl.patrykgoworowski.liftchart_common.path.PillShape
import pl.patrykgoworowski.liftchart_view.data_set.common.plus
import pl.patrykgoworowski.liftchart_view.view.dataset.DataSetView

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
                shape = CutCornerShape(topLeft = 8f.dp),
                thickness = 16f.dp,
                dynamicShader = horizontalGradient(context.flickrPink, context.trypanPurple),
            ),
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyEntryModel()

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
                            shape = CutCornerShape(topLeft = 8.dp),
                        ),
                        LineComponent(
                            color = context.byzantine,
                            thickness = 12.dp,
                            shape = PillShape,
                        ),
                        LineComponent(
                            color = context.purple,
                            thickness = 16.dp,
                            shape = CutCornerShape(topRight = 8.dp),
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

        val dataSetRenderer = composedDataSet + emptyComposedEntryModel()

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
                    shape = PillShape,
                    color = context.flickrPink,
                    margins = dimensionsOf(all = 0.5f.dp)
                ),
                componentSize = 4.dp,
            )
        }

        val dataSetRenderer = lineDataSet + emptyEntryModel()

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
                    shape = CutCornerShape(topLeft = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().byzantine,
                    thickness = 24f.dp,
                ),
                LineComponent(
                    color = requireContext().trypanPurple,
                    thickness = 16f.dp,
                    shape = CutCornerShape(topRight = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Grouped,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyEntryModel()

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
                    shape = CutCornerShape(bottomRight = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().color { R.color.byzantine },
                    thickness = 16f.dp,
                ),
                LineComponent(
                    color = requireContext().color { R.color.trypan_purple },
                    thickness = 16f.dp,
                    shape = CutCornerShape(topLeft = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Stack,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyEntryModel()

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
