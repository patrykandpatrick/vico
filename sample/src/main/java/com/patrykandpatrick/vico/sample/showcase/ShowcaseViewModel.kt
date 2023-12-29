/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.showcase

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.PieChartModelProducer
import com.patrykandpatrick.vico.core.util.RandomCartesianModelGenerator
import com.patrykandpatrick.vico.core.util.RandomPieModelGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class ShowcaseViewModel : ViewModel() {
    internal val modelProducer1 = CartesianChartModelProducer.build()
    internal val modelProducer2 = CartesianChartModelProducer.build()
    internal val modelProducer3 = CartesianChartModelProducer.build()
    internal val modelProducer4 = CartesianChartModelProducer.build()
    internal val modelProducer5 = CartesianChartModelProducer.build()
    internal val modelProducer6 = CartesianChartModelProducer.build()
    internal val modelProducer10 = CartesianChartModelProducer.build()
    internal val pieModelProducer1 = PieChartModelProducer.build()

    var uiSystem by mutableStateOf(UISystem.Compose)
        private set

    init {
        viewModelScope.launch(Dispatchers.Default) {
            while (currentCoroutineContext().isActive) {
                val singleSeriesLineLayerModelPartial =
                    RandomCartesianModelGenerator.getRandomLineLayerModelPartial()
                val tripleSeriesColumnLayerModelPartial =
                    RandomCartesianModelGenerator.getRandomColumnLayerModelPartial(seriesCount = 3)
                modelProducer1.tryRunTransaction { add(singleSeriesLineLayerModelPartial) }
                modelProducer2.tryRunTransaction {
                    add(RandomCartesianModelGenerator.getRandomColumnLayerModelPartial())
                }
                modelProducer3.tryRunTransaction {
                    add(tripleSeriesColumnLayerModelPartial)
                    add(singleSeriesLineLayerModelPartial)
                }
                modelProducer4.tryRunTransaction { add(tripleSeriesColumnLayerModelPartial) }
                modelProducer5.tryRunTransaction {
                    add(RandomCartesianModelGenerator.getRandomLineLayerModelPartial(seriesCount = 3))
                }
                modelProducer6.tryRunTransaction {
                    add(RandomCartesianModelGenerator.getRandomLineLayerModelPartial(y = -10f..20f))
                }
                modelProducer10.tryRunTransaction {
                    add(RandomCartesianModelGenerator.getRandomCandlestickLayerModelPartial())
                }
                pieModelProducer1.tryRunTransaction {
                    set(RandomPieModelGenerator.getRandomPartial())
                }
                delay(UPDATE_FREQUENCY)
            }
        }
    }

    fun setUISystem(uiSystem: UISystem) {
        this.uiSystem = uiSystem
    }

    private companion object {
        const val UPDATE_FREQUENCY = 2000L
    }
}
