package pl.patrykgoworowski.liftchart.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import pl.patrykgoworowski.liftchart_common.data_set.axis.*
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.data_set.axis.formatter.PercentageFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.extension.plusAssign
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_compose.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.path.CutCornerBarPath
import java.util.*

class ComposeShowcaseFragment : Fragment() {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    private val chartModifier = Modifier
        //.fillMaxWidth()
        .wrapContentWidth()
        //.background(Color.Black)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView

        val axisMap = EnumMap<Position, AxisRenderer>(Position::class.java)
        axisMap += VerticalAxis(StartAxis).apply {
            valueFormatter = PercentageFormatAxisValueFormatter()
        }
        axisMap += VerticalAxis(EndAxis)
        axisMap += HorizontalAxis(TopAxis)
        axisMap += HorizontalAxis(BottomAxis).apply {
            valueFormatter = DecimalFormatAxisValueFormatter()
        }

        composeView.setContent {
            MainTheme {
                ScrollableColumn {

                    Spacer(modifier = Modifier.preferredHeight(24.dp))

                    BarDataSet(
                        singleEntryCollection = viewModel.entries,
                        modifier = chartModifier,
                        color = flickrPink,
                        shape = CutCornerBarPath(topLeft = 8.dp),
                        axisMap = axisMap,
                    )

                    Spacer(modifier = Modifier.preferredHeight(24.dp))

                    MergedBarDataSet(
                        modifier = chartModifier,
                        multiEntryCollection = viewModel.multiEntries,
                        colors = listOf(flickrPink, byzantine, trypanPurple),
                        shapes = listOf(
                            CutCornerBarPath(bottomRight = 8.dp),
                            RectShape(),
                            CutCornerBarPath(topLeft = 8.dp),
                        ),
                        axisMap = axisMap,
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