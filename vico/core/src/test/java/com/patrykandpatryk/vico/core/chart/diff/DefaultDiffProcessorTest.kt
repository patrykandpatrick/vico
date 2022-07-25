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

package com.patrykandpatryk.vico.core.chart.diff

import com.patrykandpatryk.vico.core.entry.diff.DefaultDiffProcessor
import com.patrykandpatryk.vico.core.entry.entriesOf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

public class DefaultDiffProcessorTest {

    private val processor = DefaultDiffProcessor()
    private val old = listOf(entriesOf(0f to 2f, 1f to 1f, 2f to 1f, 3f to 10f))
    private val new = listOf(entriesOf(0f to 2f, 2f to 2f, 3f to 5f))

    @Before
    public fun setEntries() {
        processor.setEntries(old = old, new = new)
    }

    @Test
    public fun `Test 0 per cent progress`() {
        Assert.assertEquals(old, processor.progressDiff(0f))
    }

    @Test
    public fun `Test 50 per cent progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 0.5f, 2f to 1.5f, 3f to 7.5f))
        Assert.assertEquals(expected, processor.progressDiff(0.5f))
    }

    @Test
    public fun `Test 100 per cent progress`() {
        Assert.assertEquals(new, processor.progressDiff(1f))
    }
}
