package pl.patrykgoworowski.liftchart.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pl.patrykgoworowski.liftchart.ui.MainTheme
import pl.patrykgoworowski.liftchart.ui.byzantine
import pl.patrykgoworowski.liftchart.ui.flickrPink
import pl.patrykgoworowski.liftchart.ui.trypanPurple
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.axis.formatter.PercentageFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_compose.component.rectComponent
import pl.patrykgoworowski.liftchart_compose.data_set.bar.ColumnChart
import pl.patrykgoworowski.liftchart_compose.data_set.bar.MergedColumnChart

class ComposeShowcaseFragment : Fragment() {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    private val chartModifier
        get() = Modifier
            //.fillMaxWidth()
            .wrapContentWidth()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView

        composeView.setContent {
            MainTheme {

                val axisManager = AxisManager(
                    VerticalAxis().apply {
                        valueFormatter = PercentageFormatAxisValueFormatter()
                        axis = null
                        tick = null
                    },
                    bottomAxis = null
                )

                Column {

                    Spacer(modifier = Modifier.height(24.dp))

                    ColumnChart(
                        singleEntryCollection = viewModel.entries,
                        modifier = chartModifier,
                        column = rectComponent(
                            color = flickrPink,
                            thickness = 16.dp,
                            shape = CutCornerShape(topStart = 8f.dp)
                        ).apply {
                                setMargins(start = 0f)
                        },
                        axisManager = axisManager,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    MergedColumnChart(
                        modifier = chartModifier,
                        mergeMode = MergeMode.Grouped,
                        multiEntryCollection = viewModel.multiEntries,
                        columns = listOf(
                            rectComponent(
                                color = flickrPink,
                                shape = CutCornerShape(topStart = 8.dp)
                            ),
                            rectComponent(color = byzantine, thickness = 24.dp),
                            rectComponent(
                                color = trypanPurple,
                                shape = CutCornerShape(topEnd = 8.dp)
                            ),
                        ),
                        innerSpacing = 4.dp,
                        spacing = 24.dp,
                        axisManager = axisManager,
                    )
                }
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