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

package pl.patrykgoworowski.vico.core

import org.junit.Test
import pl.patrykgoworowski.vico.core.dataset.diff.TestDiffAnimator
import pl.patrykgoworowski.vico.core.dataset.entry.collection.EntryList
import pl.patrykgoworowski.vico.core.entry.FloatEntry
import pl.patrykgoworowski.vico.core.entry.entriesOf
import kotlin.test.assertEquals

class EntryCollectionTests {

    private val minX = 0f
    private val maxX = 3f
    private val minY = 1f
    private val maxY = 5f

    private val entries1 = entriesOf(1 to minY, 2 to 2, maxX to 3)
    private val entries2 = entriesOf(minX to minY, 1 to maxY, maxX to minY)
    private val entries3 = entriesOf(minX to 2, 1 to 4, maxX to 3)

    private val diffAnimator = TestDiffAnimator()

    @Test
    fun `Test Min Max calculations`() {
        val entryList = EntryList(TestDiffAnimator(), false)
        entryList.setEntries(entries1, entries2, entries3)
        assertEquals(minX, entryList.minX)
        assertEquals(maxX, entryList.maxX)
        assertEquals(minY, entryList.minY)
        assertEquals(maxY, entryList.maxY)
        assertEquals(10f, entryList.stackedMaxY)
        assertEquals(2f, entryList.stackedMinY)
    }

    @Test
    fun `Test entry update while diff animation is running`() {
        val first = entriesOf(0f to 2f, 1f to 0f)
        val second = entriesOf(0f to 0f, 1f to 2f)

        val entryCollection = EntryList(diffAnimator)
        entryCollection.setEntries(first)

        fun assertEntriesAreEqual(entries: List<FloatEntry>) {
            assertEquals(entries, entryCollection.model.entryCollections[0])
        }

        diffAnimator.currentProgress = 1f
        entryCollection.setEntries(second)
        assertEntriesAreEqual(second)

        entryCollection.setEntries(first)
        diffAnimator.updateProgress(0.5f)
        assertEntriesAreEqual(entriesOf(0f to 1f, 1f to 1f))
    }
}
