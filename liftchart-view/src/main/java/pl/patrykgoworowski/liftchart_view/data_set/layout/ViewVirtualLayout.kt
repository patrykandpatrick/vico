package pl.patrykgoworowski.liftchart_view.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

public open class ViewVirtualLayout(isLTR: Boolean) : VirtualLayout(isLTR) {

    public open fun <Model: EntriesModel> getMeasuredWidth(
        dataSet: DataSetRendererWithModel<Model>,
        axisMap: Map<Position, AxisRenderer>,
    ): Int = getMeasuredWidth(dataSet, dataSet.getEntriesModel(), axisMap)

    public open fun <Model: EntriesModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetRendererWithModel<Model>,
        axisMap: Map<Position, AxisRenderer>,
    ) {
        setBounds(contentBounds, dataSet, dataSet.getEntriesModel(), axisMap)
    }

}