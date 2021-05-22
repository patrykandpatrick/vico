package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Canvas
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_MERGED_BAR_INNER_SPACING
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergeMode
import pl.patrykgoworowski.liftchart_common.data_set.bar.MergedColumnDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.MultiEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.multi.emptyMultiEntriesModel
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

class MergedColumnDataSet(
    columns: List<RectComponent>,
    spacing: Float = DEF_BAR_SPACING.dp,
    innerSpacing: Float = DEF_MERGED_BAR_INNER_SPACING.dp,
    mergeMode: MergeMode = MergeMode.Stack,
) : MergedColumnDataSetRenderer(columns, spacing, innerSpacing, mergeMode),
    DataSetRendererWithModel<MultiEntriesModel> {

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: MultiEntriesModel = emptyMultiEntriesModel()
        set(value) {
            field = value
            listeners.forEach { it() }
        }

    override fun draw(canvas: Canvas) = draw(canvas, model)

    override fun addListener(listener: UpdateRequestListener) {
        listeners += listener
    }

    override fun removeListener(listener: UpdateRequestListener) {
        listeners -= listener
    }

    override fun getMeasuredWidth(): Int = getMeasuredWidth(model)

    override fun getEntriesModel(): MultiEntriesModel = model

}