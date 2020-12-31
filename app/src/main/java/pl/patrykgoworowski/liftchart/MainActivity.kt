package pl.patrykgoworowski.liftchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.CutCornerBarPath
import pl.patrykgoworowski.liftchart_core.data_set.mergeable.plus
import pl.patrykgoworowski.liftchart_core.entry.entriesOf
import pl.patrykgoworowski.liftchart_core.extension.dp
import pl.patrykgoworowski.liftchart_view.view.LiftChartContentView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<LiftChartContentView>(R.id.chart_content).apply {
//            dataSet = BarDataSet<AnyEntry>().apply {
//                setEntries(entriesOf(0 to 1, 2 to 2, 3 to 5, 4 to 10, 6 to 8, 7 to 0.2f))
//                barPathCreator = CutCornerBarPath(topLeft = 8f.dp)
//                color = ContextCompat.getColor(this@MainActivity, R.color.teal_200)
//            } + BarDataSet<AnyEntry>().apply {
//                setEntries(entriesOf(0 to 2, 1 to 1, 3 to 5, 4 to 10, 6 to 8, 9 to 0.4f))
//                color = ContextCompat.getColor(this@MainActivity, R.color.purple_200)
//            } + BarDataSet<AnyEntry>().apply {
//                setEntries(entriesOf(0 to 2, 1 to 1, 3 to 4, 4 to 11, 6 to 8, 9 to 0.4f))
//                barPathCreator = CutCornerBarPath(topRight = 8f.dp)
//                color = ContextCompat.getColor(this@MainActivity, R.color.teal_700)
//            }
            dataSet = BarDataSet<AnyEntry>().apply {
                setEntries(entriesOf(0 to 1, 1 to 1, 2 to 5))
                barPathCreator = CutCornerBarPath(topLeft = 8f.dp)
                color = ContextCompat.getColor(this@MainActivity, R.color.teal_200)
            } + BarDataSet<AnyEntry>().apply {
                setEntries(entriesOf(0 to 2, 1 to 1, 2 to 5))
                color = ContextCompat.getColor(this@MainActivity, R.color.purple_200)
            } + BarDataSet<AnyEntry>().apply {
                setEntries(entriesOf(0 to 2, 1 to 1, 2 to 4))
                barPathCreator = CutCornerBarPath(topRight = 8f.dp)
                color = ContextCompat.getColor(this@MainActivity, R.color.teal_700)
            }
        }
    }
}