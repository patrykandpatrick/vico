/*
 * Copyright (c) 2022. Patryk Goworowski
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

package pl.patrykgoworowski.vico.core.extension

import android.graphics.RectF
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlin.math.sqrt
import org.junit.Assert
import org.junit.Before
import org.junit.Test

public class RectFExtensionsTest {

    @MockK
    private lateinit var rect: RectF

    @Before
    public fun setUp() {
        MockKAnnotations.init(this)
        every { rect.width() } answers { rect.right - rect.left }
        every { rect.height() } answers { rect.bottom - rect.top }
        every { rect.centerX() } answers { (rect.right + rect.left) / 2 }
        every { rect.centerY() } answers { (rect.bottom + rect.top) / 2 }
        every { rect.set(any(), any(), any(), any()) } answers {
            rect.left = args[0] as Float
            rect.top = args[1] as Float
            rect.right = args[2] as Float
            rect.bottom = args[3] as Float
        }
    }

    @Test
    public fun `Given RectF coordinates are rotated resulting transformation has correct dimensions`() {
        rect.set(0f, 0f, 10f, 10f)
        val squareDiagonalWidth = rect.width() * sqrt(2f)
        val originalCenterX = rect.centerX()
        val originalCenterY = rect.centerY()
        rect.rotate(45f)
        Assert.assertEquals(squareDiagonalWidth, rect.width())
        Assert.assertEquals(originalCenterX, rect.centerX())
        Assert.assertEquals(originalCenterY, rect.centerY())
    }
}
