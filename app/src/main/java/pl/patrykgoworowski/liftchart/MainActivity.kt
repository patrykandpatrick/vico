package pl.patrykgoworowski.liftchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import pl.patrykgoworowski.liftchart.extension.colors
import pl.patrykgoworowski.liftchart.ui.MainTheme
import pl.patrykgoworowski.liftchart.ui.purple200
import pl.patrykgoworowski.liftchart.ui.teal200
import pl.patrykgoworowski.liftchart.ui.teal700
import pl.patrykgoworowski.liftchart_common.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.EntryList
import pl.patrykgoworowski.liftchart_common.entry.entriesOf
import pl.patrykgoworowski.liftchart_compose.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_view.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_view.extension.dp
import pl.patrykgoworowski.liftchart_view.view.LiftChartContentView

class MainActivity : AppCompatActivity() {

    private val entries1 = entriesOf(0 to 1, 1 to 2, 2 to 5)
    private val entries2 = entriesOf(0f to 2, 1 to 1, 2 to 5)
    private val entries3 = entriesOf(0 to 2, 1 to 1, 2 to 4)

    private val dataSet by lazy {
        MergedBarDataSet<AnyEntry>(
            entryCollections = listOf(EntryList(entries1), EntryList(entries2), EntryList(entries3)),
            colors = colors { intArrayOf(R.color.teal_200, R.color.purple_200, R.color.teal_700) },
            barPathCreators = listOf(
                CutCornerBarPath(topLeft = 8f.dp),
                CutCornerBarPath(bottomRight = 8f.dp),
            )
        )
//        BarDataSet<AnyEntry>(
//            EntryList(entries1),
//            barPathCreator = CutCornerBarPath(topLeft = 8f.dp),
//            color = ContextCompat.getColor(this@MainActivity, R.color.teal_200)
//        )
    }

    private val chartModifier = Modifier
        .fillMaxWidth()
        .preferredHeight(Dp(200f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                ScrollableColumn {

                    Header(text = "View based LiftChart")

                    Spacer(modifier = Modifier.preferredHeight(Dp(8f)))

                    AndroidView(
                        viewBlock = { context ->
                            LiftChartContentView(context)
                        },
                        modifier = chartModifier,
                        update = { contentView ->
                            contentView.dataSet = dataSet
                        })

                    Header(text = "Jetpack Compose based LiftChart")

                    Spacer(modifier = Modifier.preferredHeight(Dp(8f)))

                    BarDataSet(
                        entryCollection = EntryList(entries1),
                        modifier = chartModifier,
                        color = teal200,
                        barPathCreator = CutCornerBarPath(topLeft = 8f.dp)
                    )

                    Spacer(modifier = Modifier.preferredHeight(Dp(24f)))

                    MergedBarDataSet(
                        modifier = chartModifier,
                        entryCollections = listOf(EntryList(entries1), EntryList(entries2), EntryList(entries3)),
                        colors = listOf(teal200, purple200, teal700),
                        barPathCreators = listOf(
                            CutCornerBarPath(topLeft = 8f.dp),
                            CutCornerBarPath(bottomRight = 8f.dp),
                        )
                    )
                }
            }
        }
    }

    @Composable
    fun Header(modifier: Modifier = Modifier, text: String) {
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface,
            modifier = modifier
                .padding(Dp(16f))
        )
    }

}