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
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.axis.formatter.DecimalFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.formatter.PercentageFormatAxisValueFormatter
import pl.patrykgoworowski.liftchart_common.axis.horizontal.HorizontalAxis
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.entry.collectAsFlow
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_view.data_set.bar.ColumnDataSet
import pl.patrykgoworowski.liftchart_view.data_set.bar.MergedColumnDataSet
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
        val axes = AxisManager(
            VerticalAxis().apply {
                valueFormatter = PercentageFormatAxisValueFormatter()
            },
            HorizontalAxis(),
            VerticalAxis(),
            HorizontalAxis().apply {
                valueFormatter = DecimalFormatAxisValueFormatter()
            }
        )

        val barDataSet = ColumnDataSet(
            column = RectComponent(
                color = requireContext().color { R.color.flickr_pink },
                shape = CutCornerBarPath(topLeft = 8f.dp),
                thickness = 16f.dp
            )
        )

        viewModel.entries.collectAsFlow
            .onEach { barDataSet.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = barDataSet
            axisManager = axes
        }
    }

    private fun setUpMergedBar(dataSetView: DataSetView) {

        val axes = AxisManager(
            VerticalAxis().apply {
                valueFormatter = PercentageFormatAxisValueFormatter()
            },
            HorizontalAxis(),
            VerticalAxis(),
            HorizontalAxis().apply {
                valueFormatter = DecimalFormatAxisValueFormatter()
            }
        )

        val mergedBarDataSet = MergedColumnDataSet(
            columns = listOf(
                RectComponent(
                    color = requireContext().color { R.color.flickr_pink },
                    thickness = 16f.dp,
                    shape = CutCornerBarPath(topLeft = 8f.dp)
                ),
                RectComponent(
                    color = requireContext().color { R.color.byzantine },
                    thickness = 16f.dp,
                ),
                RectComponent(
                    color = requireContext().color { R.color.trypan_purple },
                    thickness = 16f.dp,
                    shape = CutCornerBarPath(topRight = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Stack
        )

        viewModel.multiEntries.collectAsFlow
            .onEach { mergedBarDataSet.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = mergedBarDataSet
            axisManager = axes
        }
    }

}