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

package pl.patrykgoworowski.vico.core

import kotlin.test.assertEquals
import org.junit.Test
import pl.patrykgoworowski.vico.core.entry.ChartEntryModelProducer
import pl.patrykgoworowski.vico.core.entry.diff.DefaultDiffProcessor
import pl.patrykgoworowski.vico.core.entry.entriesOf

public class ChartModelProducerTests {

    private val minX = 0f
    private val maxX = 3f
    private val minY = 1f
    private val maxY = 5f

    private val entries1 = entriesOf(1 to minY, 2 to 2, maxX to 3)
    private val entries2 = entriesOf(minX to minY, 1 to maxY, maxX to minY)
    private val entries3 = entriesOf(minX to 2, 1 to 4, maxX to 3)

    @Test
    public fun `Test Min Max calculations`() {
        val entryList = ChartEntryModelProducer(entries1, entries2, entries3).getModel()
        assertEquals(minX, entryList.minX)
        assertEquals(maxX, entryList.maxX)
        assertEquals(minY, entryList.minY)
        assertEquals(maxY, entryList.maxY)
        assertEquals(10f, entryList.stackedMaxY)
    }

    @Test
    public fun `Test entry update while diff animation is running`() {
        val first = entriesOf(0f to 2f, 1f to 0f)
        val second = entriesOf(0f to 0f, 1f to 2f)

        val diffProcessor = DefaultDiffProcessor()
        diffProcessor.setEntries(listOf(first))

        assertEquals(first, diffProcessor.progressDiff(1f)[0])

        diffProcessor.setEntries(listOf(second))
        assertEquals(entriesOf(0f to 1f, 1f to 1f), diffProcessor.progressDiff(.5f)[0])
    }
}
