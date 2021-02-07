package pl.patrykgoworowski.liftchart_common

import org.junit.Test
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import kotlin.test.assertEquals

class MultiEntryCollectionTests {

    private val minX = 0f
    private val maxX = 3f
    private val minY = 1f
    private val maxY = 5f

    private val entries1 = entriesOf(              1 to minY, 2 to 2, maxX to 3)
    private val entries2 = entriesOf(minX to minY, 1 to maxY,         maxX to minY)
    private val entries3 = entriesOf(minX to 2,    1 to 4,            maxX to 3)

    private val multiEntryCollection = MultiEntryList(entries1, entries2, entries3)

 @Test
 fun testMinMax() {
     assertEquals(minX, multiEntryCollection.minX)
     assertEquals(maxX, multiEntryCollection.maxX)
     assertEquals(minY, multiEntryCollection.minY)
     assertEquals(maxY, multiEntryCollection.maxY)
     assertEquals(10f, multiEntryCollection.stackedMaxY)
     assertEquals(2f, multiEntryCollection.stackedMinY)
 }

}