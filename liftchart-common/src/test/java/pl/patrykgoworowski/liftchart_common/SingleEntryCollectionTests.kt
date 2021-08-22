package pl.patrykgoworowski.liftchart_common

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.diff.TestDiffAnimator
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.FloatEntry
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_common.entry.entryOf

class SingleEntryCollectionTests {

    private val entry1 = entryOf(1f, 1f)
    private val entry2 = entryOf(0, 0)
    private val entries3 = entriesOf(3 to 4, 8 to 28)

    private val diffAnimator = TestDiffAnimator()

    @Test
    fun testCollectionModifications() {
        val entryCollection = SingleEntryList(diffAnimator, false)

        fun assertSize(size: Int) {
            assertEquals(size, entryCollection.data.size)
        }

        assertSize(0)

        entryCollection += entry1
        entryCollection += entry2
        assertSize(2)

        entryCollection += entries3
        assertSize(4)

        entryCollection -= entry1
        entryCollection -= entry2
        assertSize(2)

        entryCollection -= entries3
        assertSize(0)
    }

    @Test
    fun testSizes() {
        val entryCollection = SingleEntryList(diffAnimator, false)

        entryCollection += entry1

        assertEquals(entryCollection.minX, 1f)
        assertEquals(entryCollection.maxX, 1f)
        assertEquals(entryCollection.minY, 1f)
        assertEquals(entryCollection.maxY, 1f)

        entryCollection += entry2
        entryCollection += entries3

        assertEquals(entryCollection.minX, 0f)
        assertEquals(entryCollection.maxX, 8f)
        assertEquals(entryCollection.minY, 0f)
        assertEquals(entryCollection.maxY, 28f)
    }

    @Test
    fun `Test entry update while diff animation is running`() {
        val first = entriesOf(0f to 2f, 1f to 0f)
        val second = entriesOf(0f to 0f, 1f to 2f)

        val entryCollection = SingleEntryList(diffAnimator)
        entryCollection.setEntries(first)

        fun assertEntriesAreEqual(entries: List<FloatEntry>) {
            assertEquals(entries, entryCollection.model.entries)
        }

        diffAnimator.currentProgress = 1f
        entryCollection.setEntries(second)
        assertEntriesAreEqual(second)

        entryCollection.setEntries(first)
        diffAnimator.updateProgress(0.5f)
        assertEntriesAreEqual(entriesOf(0f to 1f, 1f to 1f))
    }

}