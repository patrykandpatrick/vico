package pl.patrykgoworowski.liftchart_common

import org.junit.Assert.assertEquals
import org.junit.Test
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_common.entry.entryOf

class SingleEntryCollectionTests {

    private val entry1 = entryOf(1f, 1f)
    private val entry2 = entryOf(0, 0)
    private val entries3 = entriesOf(3 to 4, 8 to 28)

    @Test
    fun testCollectionModifications() {
        val entryCollection = SingleEntryList<AnyEntry>()

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
        val entryCollection = SingleEntryList<AnyEntry>()

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

}