/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.sample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.patrykandpatryk.vico.R
import com.patrykandpatryk.vico.core.axis.vertical.VerticalAxis
import com.patrykandpatryk.vico.core.chart.composed.ComposedChart
import com.patrykandpatryk.vico.core.chart.decoration.ThresholdLine
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.component.dimension.setMargins
import com.patrykandpatryk.vico.core.component.shape.ShapeComponent
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.extension.copyColor
import com.patrykandpatryk.vico.core.marker.Marker
import com.patrykandpatryk.vico.databinding.FragmentViewShowcaseBinding
import com.patrykandpatryk.vico.sample.component.getDottedShader
import com.patrykandpatryk.vico.sample.component.getThresholdLineLabel
import com.patrykandpatryk.vico.sample.extensions.getMarker
import com.patrykandpatryk.vico.sample.extensions.navigationBarInsets
import com.patrykandpatryk.vico.sample.extensions.resolveColorAttribute
import com.patrykandpatryk.vico.sample.extensions.setUpChart
import com.patrykandpatryk.vico.sample.util.Tokens
import com.patrykandpatryk.vico.sample.viewmodel.ShowcaseViewModel
import com.patrykandpatryk.vico.view.dimensions.dimensionsOf

internal class ViewShowcaseFragment : Fragment() {

    private var _binding: FragmentViewShowcaseBinding? = null
    private val binding get() = _binding!!

    private val showcaseViewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentViewShowcaseBinding.inflate(
            inflater,
            container,
            false,
        )

        with(binding) {
            wrapper.setOnApplyWindowInsetsListener { view, insets ->
                view.updatePadding(bottom = view.paddingBottom + insets.navigationBarInsets.bottom)
                insets
            }
        }

        with(requireContext().getMarker()) {
            setUpColumnChart(marker = this)
            setUpLineChart(marker = this)
            setUpStackedColumnChart(marker = this)
            setUpComposedChart(marker = this)
            setUpGroupedColumnChart(marker = this)
            setUpLineChartWithLabelsInside(marker = this)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        with(binding) {
            columnChart.entryProducer = null
            lineChart.entryProducer = null
            stackedColumnChart.entryProducer = null
            composedChart.entryProducer = null
            groupedColumnChart.entryProducer = null
            lineChartWithLabelsInside.entryProducer = null
        }
        _binding = null
    }

    private fun setUpColumnChart(marker: Marker) {
        binding.columnChart.setUpChart(
            chartModelProducer = showcaseViewModel.chartEntryModelProducer,
            marker = marker,
        ) {
            chart?.addPersistentMarker(
                x = Tokens.ColumnChart.PERSISTENT_MARKER_X,
                marker = marker,
            )
        }
    }

    private fun setUpLineChart(marker: Marker) {
        binding.lineChart.setUpChart(
            chartModelProducer = showcaseViewModel.chartEntryModelProducer,
            marker = marker,
        ) {
            val secondaryColor = context.resolveColorAttribute(R.attr.colorSecondary)

            chart?.addDecoration(
                decoration = ThresholdLine(
                    thresholdValue = Tokens.LineChart.THRESHOLD_VALUE,
                    labelComponent = getThresholdLineLabel(
                        color = secondaryColor,
                        backgroundColor = context.resolveColorAttribute(R.attr.colorSurface),
                        strokeColor = secondaryColor,
                    ),
                    lineComponent = ShapeComponent(
                        strokeColor = context.resolveColorAttribute(R.attr.colorSecondary),
                        strokeWidthDp = Tokens.LineChart.THRESHOLD_LINE_STROKE_WIDTH_DP,
                    )
                )
            )
        }
    }

    private fun setUpStackedColumnChart(marker: Marker) {
        binding.stackedColumnChart.setUpChart(
            chartModelProducer = showcaseViewModel.multiChartEntryModelProducer,
            marker = marker,
        )
    }

    private fun setUpComposedChart(marker: Marker) {
        binding.composedChart.setUpChart(
            chartModelProducer = showcaseViewModel.composedChartEntryModelProducer,
            marker = marker,
        ) {
            val secondaryColor = context.resolveColorAttribute(R.attr.colorSecondary)
            val lineChart = (chart as ComposedChart).charts[1] as LineChart

            with(lineChart) {
                lines = listOf(
                    LineChart.LineSpec(
                        lineColor = secondaryColor,
                        lineBackgroundShader = getDottedShader(
                            dotColor = secondaryColor.copyColor(alpha = Tokens.ComposedChart.SHADER_ALPHA),
                        ),
                    ),
                )
            }
        }
    }

    private fun setUpGroupedColumnChart(marker: Marker) {
        binding.groupedColumnChart.setUpChart(
            chartModelProducer = showcaseViewModel.multiChartEntryModelProducer,
            marker = marker,
        ) {
            val primaryColor = context.resolveColorAttribute(R.attr.colorPrimary)
            val tokens = Tokens.GroupedColumnChart

            chart?.addDecoration(
                decoration = ThresholdLine(
                    thresholdRange = tokens.THRESHOLD_START..tokens.THRESHOLD_END,
                    labelComponent = getThresholdLineLabel(
                        color = primaryColor,
                        backgroundColor = context.resolveColorAttribute(R.attr.colorSurface),
                        strokeColor = primaryColor,
                    ),
                    lineComponent = ShapeComponent(
                        color = primaryColor.copyColor(alpha = tokens.THRESHOLD_LINE_BACKGROUND_ALPHA),
                    ),
                )
            )
        }
    }

    private fun setUpLineChartWithLabelsInside(marker: Marker) {
        binding.lineChartWithLabelsInside.setUpChart(
            chartModelProducer = showcaseViewModel.chartEntryModelProducer,
            marker = marker,
        ) {
            val tokens = Tokens.LineChartWithLabelsInside

            (chart as LineChart).lines = listOf(
                LineChart.LineSpec(lineBackgroundShader = null)
            )
            with(startAxis as VerticalAxis) {
                label?.apply {
                    color = context.resolveColorAttribute(R.attr.colorOnSecondary)
                    setMargins(margins = dimensionsOf(verticalDp = tokens.LABEL_VERTICAL_MARGIN_DP))
                    padding = dimensionsOf(
                        horizontalDp = tokens.LABEL_HORIZONTAL_PADDING_DP,
                        verticalDp = tokens.LABEL_VERTICAL_PADDING_DP,
                    )
                    background = ShapeComponent(
                        shape = Shapes.pillShape,
                        color = context.resolveColorAttribute(R.attr.colorSecondary),
                    )
                }
            }
        }
    }
}
