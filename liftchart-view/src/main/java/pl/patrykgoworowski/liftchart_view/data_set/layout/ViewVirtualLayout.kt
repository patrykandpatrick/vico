package pl.patrykgoworowski.liftchart_view.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.layout.VirtualLayout
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel

open class ViewVirtualLayout(isLTR: Boolean) : VirtualLayout(isLTR) {

    open fun <Model : EntriesModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetRendererWithModel<Model>,
        dataSetModel: DataSetModel,
        axisManager: AxisManager,
        vararg dataSetInsetter: DataSetInsetter?,
    ) {
        setBounds(
            contentBounds, dataSet, dataSet.getEntriesModel(), dataSetModel, axisManager,
            *dataSetInsetter
        )
    }

}