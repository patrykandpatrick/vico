package pl.patrykgoworowski.liftchart.showcase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pl.patrykgoworowski.liftchart.ui.MainTheme
import pl.patrykgoworowski.liftchart.ui.purple200
import pl.patrykgoworowski.liftchart.ui.teal200
import pl.patrykgoworowski.liftchart.ui.teal700
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_compose.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_compose.data_set.bar.path.CutCornerBarPath

class ComposeShowcaseFragment: Fragment() {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    private val chartModifier = Modifier
        //.fillMaxWidth()
        .wrapContentWidth()
        .preferredHeight(Dp(200f))
        .background(Color.Black)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView
        composeView.setContent {
            MainTheme {
                ScrollableColumn {

                    Spacer(modifier = Modifier.preferredHeight(8.dp))

                    BarDataSet(
                        singleEntryCollection = viewModel.entries,
                        modifier = chartModifier,
                        color = teal200,
                        barPathCreator = CutCornerBarPath(topLeft = 8.dp)
                    )

                    Spacer(modifier = Modifier.preferredHeight(24.dp))

                    MergedBarDataSet(
                        modifier = chartModifier,
                        multiEntryCollection = viewModel.multiEntries,
                        colors = listOf(teal200, purple200, teal700),
                        barPathCreators = listOf(
                            CutCornerBarPath(bottomRight = 8.dp),
                            DefaultBarPath(),
                            CutCornerBarPath(topLeft = 8.dp)
                        )
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