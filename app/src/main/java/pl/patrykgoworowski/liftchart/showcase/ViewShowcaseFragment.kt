package pl.patrykgoworowski.liftchart.showcase

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import pl.patrykgoworowski.liftchart.R
import pl.patrykgoworowski.liftchart.component.view.getMarkerComponent
import pl.patrykgoworowski.liftchart.databinding.FragmentViewBinding
import pl.patrykgoworowski.liftchart.extension.byzantine
import pl.patrykgoworowski.liftchart.extension.color
import pl.patrykgoworowski.liftchart.extension.flickrPink
import pl.patrykgoworowski.liftchart.extension.trypanPurple
import pl.patrykgoworowski.liftchart_common.axis.horizontal.bottomAxis
import pl.patrykgoworowski.liftchart_common.axis.vertical.startAxis
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.shape.shader.componentShader
import pl.patrykgoworowski.liftchart_common.component.shape.shader.horizontalGradient
import pl.patrykgoworowski.liftchart_common.component.shape.shader.verticalGradient
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSet
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.entry.collectAsFlow
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.emptyMultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.line.LineDataSet
import pl.patrykgoworowski.liftchart_common.dimensions.dimensionsOf
import pl.patrykgoworowski.liftchart_common.extension.copyColor
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.path.cutCornerShape
import pl.patrykgoworowski.liftchart_common.path.pillShape
import pl.patrykgoworowski.liftchart_view.data_set.common.plus
import pl.patrykgoworowski.liftchart_view.view.dataset.DataSetView

class ViewShowcaseFragment : Fragment(R.layout.fragment_view) {

    private val viewModel: ShowcaseViewModel by lazy {
        ViewModelProvider(requireActivity()).get(ShowcaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentViewBinding.bind(view)

        val marker = getMarkerComponent(view.context)

        setUpBar(binding.barChart, marker)
        setUpLineChart(binding.lineChart, marker)
        setUpGroupedBar(binding.groupedColumnChart, marker)
        setUpStackedBar(binding.stackedColumnChart, marker)
    }

    private fun setUpBar(dataSetView: DataSetView, marker: Marker?) {
        val context = dataSetView.context

        val columnDataSet = ColumnDataSet(
            column = LineComponent(
                color = context.flickrPink,
                shape = cutCornerShape(topLeft = 8f.dp),
                thickness = 16f.dp,
                dynamicShader = horizontalGradient(context.flickrPink, context.trypanPurple),
            ),
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyMultiEntriesModel()

        viewModel.entries.collectAsFlow
            .onEach { dataSetRenderer.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpLineChart(dataSetView: DataSetView, marker: Marker?) {
        val context = dataSetView.context

        val lineDataSet = LineDataSet(
            pointSize = 10.dp,
            lineColor = context.flickrPink
        ).apply {
            isHorizontalScrollEnabled = true
            lineBackgroundShader = verticalGradient(
                context.flickrPink.copyColor(alpha = 128),
                Color.TRANSPARENT,
            )
            lineBackgroundShader = componentShader(
                component = ShapeComponent(
                    shape = pillShape(),
                    color = context.flickrPink,
                    margins = dimensionsOf(all = 0.5f.dp)
                ),
                componentSize = 4.dp,
            )
        }

        val dataSetRenderer = lineDataSet + emptyMultiEntriesModel()

        viewModel.entries.collectAsFlow
            .onEach { dataSetRenderer.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpGroupedBar(dataSetView: DataSetView, marker: Marker?) {
        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = requireContext().flickrPink,
                    thickness = 16f.dp,
                    shape = cutCornerShape(topLeft = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().byzantine,
                    thickness = 24f.dp,
                ),
                LineComponent(
                    color = requireContext().trypanPurple,
                    thickness = 16f.dp,
                    shape = cutCornerShape(topRight = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Grouped,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyMultiEntriesModel()

        viewModel.multiEntries.collectAsFlow
            .onEach { dataSetRenderer.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

    private fun setUpStackedBar(dataSetView: DataSetView, marker: Marker?) {
        val columnDataSet = ColumnDataSet(
            columns = listOf(
                LineComponent(
                    color = requireContext().color { R.color.flickr_pink },
                    thickness = 16f.dp,
                    shape = cutCornerShape(bottomRight = 8f.dp)
                ),
                LineComponent(
                    color = requireContext().color { R.color.byzantine },
                    thickness = 16f.dp,
                ),
                LineComponent(
                    color = requireContext().color { R.color.trypan_purple },
                    thickness = 16f.dp,
                    shape = cutCornerShape(topLeft = 8f.dp)
                ),
            ),
            mergeMode = MergeMode.Stack,
            spacing = 24.dp,
            innerSpacing = 4.dp,
        ).apply {
            isHorizontalScrollEnabled = true
        }

        val dataSetRenderer = columnDataSet + emptyMultiEntriesModel()

        viewModel.multiEntries.collectAsFlow
            .onEach { dataSetRenderer.model = it }
            .launchIn(lifecycleScope)

        dataSetView.apply {
            dataSet = dataSetRenderer
            startAxis = startAxis()
            bottomAxis = bottomAxis()
            this.marker = marker
        }
    }

}