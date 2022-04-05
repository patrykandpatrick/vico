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

package com.patrykandpatryk.vico.core

import org.junit.Test
import com.patrykandpatryk.vico.core.extension.forEachIndexedExtended
import kotlin.test.assertEquals
import kotlin.test.fail

public class CollectionExtensionsTest {

    private val format = "index=%d, isFirst=%s, isLast=%s, value=%d"

    @Test
    public fun `test forEachIndexedExtended with 1 value`() {
        (0 until 1).forEachIndexedExtended { index, isFirst, isLast, int ->
            val actual = format.format(index, isFirst, isLast, int)
            when (int) {
                0 -> assertEquals(
                    expected = format.format(0, true, true, 0),
                    actual = actual
                )
                else -> fail("Unexpected value")
            }
        }
    }

    @Test
    public fun `test forEachIndexedExtended with 2 values`() {
        (0 until 2).forEachIndexedExtended { index, isFirst, isLast, int ->
            val actual = format.format(index, isFirst, isLast, int)
            when (int) {
                0 -> assertEquals(
                    expected = format.format(0, true, false, 0),
                    actual = actual
                )
                1 -> assertEquals(
                    expected = format.format(1, false, true, 1),
                    actual = actual
                )
                else -> fail("Unexpected value")
            }
        }
    }

    @Test
    public fun `test forEachIndexedExtended with 3 values`() {
        (0 until 3).forEachIndexedExtended { index, isFirst, isLast, int ->
            val actual = format.format(index, isFirst, isLast, int)
            when (int) {
                0 -> assertEquals(
                    expected = format.format(0, true, false, 0),
                    actual = actual
                )
                1 -> assertEquals(
                    expected = format.format(1, false, false, 1),
                    actual = actual
                )
                2 -> assertEquals(
                    expected = format.format(2, false, true, 2),
                    actual = actual
                )
                else -> fail("Unexpected value")
            }
        }
    }
}
