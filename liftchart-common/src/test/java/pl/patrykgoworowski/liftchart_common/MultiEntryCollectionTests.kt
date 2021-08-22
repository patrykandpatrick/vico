package pl.patrykgoworowski.liftchart_common

import org.junit.Test
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.TestDiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.entry.FloatEntry
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import kotlin.test.assertEquals

class MultiEntryCollectionTests {

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
        val multiEntryCollection = MultiEntryList(TestDiffAnimator(), false)
        multiEntryCollection.setEntries(entries1, entries2, entries3)
        assertEquals(minX, multiEntryCollection.minX)
        assertEquals(maxX, multiEntryCollection.maxX)
        assertEquals(minY, multiEntryCollection.minY)
        assertEquals(maxY, multiEntryCollection.maxY)
        assertEquals(10f, multiEntryCollection.stackedMaxY)
        assertEquals(2f, multiEntryCollection.stackedMinY)
    }

    @Test
    fun `Test entry update while diff animation is running`() {
        val first = entriesOf(0f to 2f, 1f to 0f)
        val second = entriesOf(0f to 0f, 1f to 2f)

        val entryCollection = MultiEntryList(diffAnimator)
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