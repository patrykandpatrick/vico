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

package pl.patrykgoworowski.vico.core.chart.diff

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import pl.patrykgoworowski.vico.core.entry.diff.DefaultDiffProcessor
import pl.patrykgoworowski.vico.core.entry.entriesOf

public class DefaultDiffProcessorTest {

    private val processor = DefaultDiffProcessor()

    @Before
    public fun setEntries() {
        processor.setEntries(
            old = listOf(entriesOf(0f to 2f, 2f to 2f, 3f to 5f)),
            new = listOf(entriesOf(0f to 2f, 1f to 1f, 2f to 1f, 3f to 10f))
        )
    }

    @Test
    public fun `Test 0% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 0f, 2f to 2f, 3f to 5f))
        Assert.assertEquals(
            expected, processor.progressDiff(0f)
        )
    }

    @Test
    public fun `Test 50% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 0.5f, 2f to 1.5f, 3f to 7.5f))
        Assert.assertEquals(
            expected, processor.progressDiff(0.5f)
        )
    }

    @Test
    public fun `Test 100% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 1f, 2f to 1f, 3f to 10f))
        Assert.assertEquals(
            expected, processor.progressDiff(1f)
        )
    }
}
