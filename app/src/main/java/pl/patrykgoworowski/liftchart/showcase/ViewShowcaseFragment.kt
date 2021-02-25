package pl.patrykgoworowski.liftchart.showcase

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.liftchart.R
import pl.patrykgoworowski.liftchart.databinding.FragmentViewBinding
import pl.patrykgoworowski.liftchart.extension.color
import pl.patrykgoworowski.liftchart.extension.colors
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.axis.*
import pl.patrykgoworowski.liftchart_common.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.PercentageFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.entry.collectAsFlow
import pl.patrykgoworowski.liftchart_common.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_view.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_view.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_view.extension.dp
import pl.patrykgoworowski.liftchart_view.view.dataset.DataSetView


class ViewShowcaseFragment : Fragment(R.layout.fragment_view) {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentViewBinding.bind(view)

        setUpBar(binding.barChart)
        setUpMergedBar(binding.mergedBarChart)
    }

    private fun setUpBar(dataSetView: DataSetView) {
        val startAxis = VerticalAxis(StartAxis).apply {
            valueFormatter = PercentageFormatAxisValueFormatter()
        }
        val endAxis = VerticalAxis(EndAxis)
        val topAxis = HorizontalAxis(TopAxis)
        val bottomAxis = HorizontalAxis(BottomAxis).apply {
            valueFormatter = DecimalFormatAxisValueFormatter()
        }

        val barDataSet = BarDataSet<AnyEntry>(
            color = requireContext().color { R.color.flickr_pink },
            shape = CutCornerBarPath(topLeft = 8f.dp)
        )

        viewModel.entries.collectAsFlow
            .onEach { barDataSet.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = barDataSet
            addAxis(startAxis)
            addAxis(endAxis)
            addAxis(topAxis)
            addAxis(bottomAxis)
        }
    }

    private fun setUpMergedBar(dataSetView: DataSetView) {

        val startAxis = VerticalAxis(StartAxis).apply {
            valueFormatter = PercentageFormatAxisValueFormatter()
        }
        val endAxis = VerticalAxis(EndAxis)
        val topAxis = HorizontalAxis(TopAxis)
        val bottomAxis = HorizontalAxis(BottomAxis).apply {
            valueFormatter = DecimalFormatAxisValueFormatter()
        }

        val mergedBarDataSet = MergedBarDataSet<AnyEntry>(
            colors = requireContext().colors {
                intArrayOf(
                    R.color.flickr_pink,
                    R.color.byzantine,
                    R.color.trypan_purple
                )
            },
            shapes = listOf(
                CutCornerBarPath(bottomRight = 8f.dp),
                RectShape(),
                CutCornerBarPath(topLeft = 8f.dp),
            ),
            mergeMode = MergeMode.Stack
        )

        viewModel.multiEntries.collectAsFlow
            .onEach { mergedBarDataSet.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = mergedBarDataSet
            addAxis(startAxis)
            addAxis(endAxis)
            addAxis(topAxis)
            addAxis(bottomAxis)
        }
    }

}