package pl.patrykgoworowski.liftchart.showcase

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import pl.patrykgoworowski.liftchart.R
import pl.patrykgoworowski.liftchart.databinding.FragmentViewBinding
import pl.patrykgoworowski.liftchart.extension.colors
import pl.patrykgoworowski.liftchart_common.data_set.bar.path.CutCornerBarPath
import pl.patrykgoworowski.liftchart_view.data_set.bar.MergedBarDataSet
import pl.patrykgoworowski.liftchart_view.extension.dp


class ViewShowcaseFragment: Fragment(R.layout.fragment_view) {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    val dataSet by lazy {
        MergedBarDataSet(
            entryCollections = viewModel.allEntries,
            colors = requireContext().colors { intArrayOf(R.color.teal_200, R.color.purple_200, R.color.teal_700) },
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentViewBinding.bind(view)
        binding.chartContent.dataSet = dataSet
    }

}