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

package com.patrykandpatrick.vico.core.chart

import com.patrykandpatrick.vico.core.chart.dimensions.MutableHorizontalDimensions
import com.patrykandpatrick.vico.core.chart.draw.ChartDrawContext
import com.patrykandpatrick.vico.core.chart.values.ChartValuesManager
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.marker.Marker
import org.junit.Test
import java.util.LinkedHashMap
import kotlin.test.assertEquals

public class BaseChartTest {

    private val sut = object : BaseChart<ChartEntryModel>() {

        override val entryLocationMap: Map<Float, MutableList<Marker.EntryModel>> = mapOf()

        override val modelTransformerProvider: Chart.ModelTransformerProvider get() = error("stub")

        public fun getPersistentMarkers(): Map<Float, Marker> = persistentMarkers

        override fun drawChart(context: ChartDrawContext, model: ChartEntryModel) = Unit

        override fun updateChartValues(chartValuesManager: ChartValuesManager, model: ChartEntryModel, xStep: Float?) =
            Unit

        override fun updateHorizontalDimensions(
            context: MeasureContext,
            horizontalDimensions: MutableHorizontalDimensions,
            model: ChartEntryModel,
        ) = Unit
    }

    private val marker = object : Marker {}

    @Test
    public fun `The exact order of persistent markers is preserved when set to BaseChart`() {
        val map = LinkedHashMap<Float, Marker>()
        repeat(100) { index -> map[index.toFloat()] = marker }
        sut.setPersistentMarkers(map)
        map.keys.forEachIndexed { index, key -> assertEquals(key, sut.getPersistentMarkers().keys.elementAt(index)) }
    }
}
