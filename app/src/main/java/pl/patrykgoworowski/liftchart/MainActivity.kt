package pl.patrykgoworowski.liftchart

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import pl.patrykgoworowski.liftchart.ui.MainTheme
import pl.patrykgoworowski.liftchart_compose.components.ChartContent
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.CutCornerBarPath
import pl.patrykgoworowski.liftchart_core.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_core.data_set.mergeable.plus
import pl.patrykgoworowski.liftchart_core.entry.entriesOf
import pl.patrykgoworowski.liftchart_core.extension.dp
import pl.patrykgoworowski.liftchart_view.view.LiftChartContentView

class MainActivity : AppCompatActivity() {

    private val dataSet by lazy {
        (BarDataSet<AnyEntry>().apply {
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
        }).apply {
            groupMode = MergedBarDataSet.GroupMode.Stack
        }
//        BarDataSet<AnyEntry>().apply {
//            setEntries(entriesOf(0 to 2, .1f to 1, 2 to 4))
//            barPathCreator = CutCornerBarPath(topRight = 8f.dp)
//            color = ContextCompat.getColor(this@MainActivity, R.color.teal_700)
//        }
    }

    private val chartModifier = Modifier
        .fillMaxWidth()
        .preferredHeight(Dp(200f))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainTheme {
                Column {

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

                    ChartContent(
                        modifier = chartModifier,
                        dataSet = dataSet
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