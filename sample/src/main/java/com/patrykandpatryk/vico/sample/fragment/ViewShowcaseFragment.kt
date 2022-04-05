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
import com.patrykandpatryk.vico.databinding.FragmentViewShowcaseBinding
import com.patrykandpatryk.vico.sample.extensions.getMarker
import com.patrykandpatryk.vico.sample.extensions.navigationBarInsets
import com.patrykandpatryk.vico.sample.extensions.setUpChart
import com.patrykandpatryk.vico.sample.viewmodel.ShowcaseViewModel

internal class ViewShowcaseFragment : Fragment() {

    private val showcaseViewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val binding = FragmentViewShowcaseBinding.inflate(inflater, container, false)
        val marker = requireContext().getMarker()
        with(binding) {
            wrapper.setOnApplyWindowInsetsListener { view, insets ->
                view.updatePadding(bottom = view.paddingBottom + insets.navigationBarInsets.bottom)
                insets
            }
            columnChart.setUpChart(showcaseViewModel.chartEntryModelProducer, marker)
            lineChart.setUpChart(showcaseViewModel.chartEntryModelProducer, marker)
            composedChart.setUpChart(showcaseViewModel.composedChartEntryModelProducer, marker)
            groupedColumnChart.setUpChart(showcaseViewModel.multiChartEntryModelProducer, marker)
            stackedColumnChart.setUpChart(showcaseViewModel.multiChartEntryModelProducer, marker)
        }
        return binding.root
    }
}
