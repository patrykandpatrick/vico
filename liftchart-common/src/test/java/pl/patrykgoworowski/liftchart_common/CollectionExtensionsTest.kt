package pl.patrykgoworowski.liftchart_common

import org.junit.Test
import pl.patrykgoworowski.liftchart_common.extension.forEachIndexedExtended
import kotlin.test.assertEquals
import kotlin.test.fail

class CollectionExtensionsTest {

    private val format = "index=%d, isFirst=%s, isLast=%s, value=%d"

    @Test
    fun `test forEachIndexedExtended with 1 value`() {
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
    fun `test forEachIndexedExtended with 2 values`() {
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
    fun `test forEachIndexedExtended with 3 values`() {
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