package pl.patrykgoworowski.liftchart_view.data_set.bar

import android.graphics.Canvas
import android.graphics.Color.MAGENTA
import pl.patrykgoworowski.liftchart_common.AnyEntry
import pl.patrykgoworowski.liftchart_common.axis.model.AxisModel
import pl.patrykgoworowski.liftchart_common.data_set.bar.BarDataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.SingleEntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.single.emptySingleEntriesModel
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_SPACING
import pl.patrykgoworowski.liftchart_common.defaults.DEF_BAR_WIDTH
import pl.patrykgoworowski.liftchart_common.path.RectShape
import pl.patrykgoworowski.liftchart_common.path.Shape
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel
import pl.patrykgoworowski.liftchart_view.extension.dp

public open class BarDataSet<Entry: AnyEntry>(
    color: Int = MAGENTA,
    barWidth: Float = DEF_BAR_WIDTH.dp,
    barSpacing: Float = DEF_BAR_SPACING.dp,
    shape: Shape = RectShape()
) : BarDataSetRenderer<Entry>(color, barWidth, barSpacing, shape),
    DataSetRendererWithModel<SingleEntriesModel<Entry>> {

    private val listeners = ArrayList<UpdateRequestListener>()

    var model: SingleEntriesModel<Entry> = emptySingleEntriesModel()
        set(value) {
            field = value
            listeners.forEach { it() }
        }

    init {
        this.color = color
        this.barWidth = barWidth
        this.barSpacing = barSpacing
        this.shape = shape
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