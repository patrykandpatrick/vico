package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.data_set.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergedBarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.emptyMultiEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.defaults.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel
import pl.patrykgoworowski.liftchart_view.extension.dp

class MergedBarDataSet<Entry: AnyEntry>(
    barWidth: Float = DEF_BAR_WIDTH.dp,
    barSpacing: Float = DEF_BAR_SPACING.dp,
    barInnerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING.dp,
    mergeMode: MergeMode = MergeMode.Stack,
    colors: List<Int> = emptyList(),
    shapes: List<Shape> = emptyList(),
) : MergedBarDataSetRenderer<Entry>(colors, barWidth, barSpacing, barInnerSpacing),
    DataSetRendererWithModel<MultiEntriesModel<Entry>> {

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: MultiEntriesModel<Entry> = emptyMultiEntriesModel()
        set(value) {
            field = value
            listeners.forEach { it() }
        }

    init {
        setColors(colors)
        this.barPathCreators.addAll(shapes)
        this.groupMode = mergeMode
    }

    override fun draw(canvas: Canvas) = draw(canvas, model)

    override fun getAxisModel(): AxisModel = getAxisModel(model)

    override fun addListener(listener: UpdateRequestListener) {
        listeners += listener
    }

    override fun removeListener(listener: UpdateRequestListener) {
        listeners -= listener
    }

    override fun getMeasuredWidth(): Int = getMeasuredWidth(model)

    override fun getEntriesModel(): MultiEntriesModel<Entry> = model

}