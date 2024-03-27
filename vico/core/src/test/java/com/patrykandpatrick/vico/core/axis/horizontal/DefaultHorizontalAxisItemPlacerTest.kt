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

package com.patrykandpatrick.vico.core.axis.horizontal

import com.patrykandpatrick.vico.core.chart.values.ChartValues
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import kotlin.test.assertEquals

public class DefaultHorizontalAxisItemPlacerTest {
    @Test
    public fun `Ensure correct labels are measured`() {
        val itemPlacer =
            DefaultHorizontalAxisItemPlacer(
                spacing = 1,
                offset = 0,
                shiftExtremeTicks = true,
                addExtremeLabelPadding = false,
            )
        val chartValues =
            mockk<ChartValues> {
                every { minX } returns 3f
                every { xStep } returns 1.5f
                every { xLength } returns 8.7f
            }
        assertEquals(listOf(3f, 7.5f, 10.5f), itemPlacer.run { chartValues.measuredLabelValues })
    }
}
