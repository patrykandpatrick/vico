package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Canvas
import android.graphics.Color.MAGENTA
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.component.RectComponent
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.constants.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.data_set.bar.ColumnDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.emptySingleEntriesModel
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

public open class ColumnDataSet<Entry : AnyEntry>(
    column: RectComponent = RectComponent(MAGENTA, DEF_BAR_WIDTH.dp),
    spacing: Float = DEF_BAR_SPACING.dp,
) : ColumnDataSetRenderer<Entry>(column, spacing),
    DataSetRendererWithModel<SingleEntriesModel<Entry>> {

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: SingleEntriesModel<Entry> = emptySingleEntriesModel()
        set(value) {
            field = value
            listeners.forEach { it() }
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

    override fun getEntriesModel(): SingleEntriesModel<Entry> = model

}