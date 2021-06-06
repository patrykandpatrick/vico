package pl.patrykgoworowski.liftchart_view.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

public open class ViewVirtualLayout(isLTR: Boolean) : VirtualLayout(isLTR) {

    public open fun <Model: EntriesModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetRendererWithModel<Model>,
        axisManager: AxisManager,
    ) {
        setBounds(contentBounds, dataSet, dataSet.getEntriesModel(), axisManager)
    }

}