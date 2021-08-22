package pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import pl.patrykgoworowski.liftchart_common.entry.entriesOf

class DefaultDIffProcessorTest {

    private val processor = DefaultDiffProcessor()

    @Before
    fun setEntries() {
        processor.setEntries(
            old = listOf(entriesOf(0f to 2f,           2f to 2f, 3f to 5f)),
            new = listOf(entriesOf(0f to 2f, 1f to 1f, 2f to 1f, 3f to 10f))
        )
    }

    @Test
    fun `Test 0% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 0f, 2f to 2f, 3f to 5f))
        Assert.assertEquals(
            expected, processor.progressDiff(0f)
        )
    }

    @Test
    fun `Test 50% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 0.5f, 2f to 1.5f, 3f to 7.5f))
        Assert.assertEquals(
            expected, processor.progressDiff(0.5f)
        )
    }

    @Test
    fun `Test 100% progress`() {
        val expected = listOf(entriesOf(0f to 2f, 1f to 1f, 2f to 1f, 3f to 10f))
        Assert.assertEquals(
            expected, processor.progressDiff(1f)
        )
    }

}