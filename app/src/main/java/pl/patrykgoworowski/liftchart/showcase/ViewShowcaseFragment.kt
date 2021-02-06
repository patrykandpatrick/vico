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
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.DefaultBarPath
import pl.patrykgoworowski.liftchart_common.data_set.entry.collectAsFlow
import pl.patrykgoworowski.liftchart_view.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_view.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_view.extension.dp


class ViewShowcaseFragment : Fragment(R.layout.fragment_view) {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentViewBinding.bind(view)

        val barDataSet = BarDataSet<AnyEntry>(
            color = requireContext().color { R.color.teal_200 },
            barPathCreator = CutCornerBarPath(topLeft = 8f.dp)
        )

        viewModel.entries.collectAsFlow
            .onEach { barDataSet.model = it }
            .launchIn(lifecycleScope)

        binding.barChart.dataSet = barDataSet

        val mergedBarDataSet = MergedBarDataSet<AnyEntry>(
            colors = requireContext().colors {
                intArrayOf(
                    R.color.teal_200,
                    R.color.purple_200,
                    R.color.teal_700
                )
            },
            barPathCreators = listOf(
                CutCornerBarPath(bottomRight = 8f.dp),
                DefaultBarPath(),
                CutCornerBarPath(topLeft = 8f.dp),
            ),
            mergeMode = MergeMode.Grouped
        )

        viewModel.multiEntries.collectAsFlow
            .onEach { mergedBarDataSet.model = it }
            .launchIn(lifecycleScope)

        binding.mergedBarChart.dataSet = mergedBarDataSet
    }

}