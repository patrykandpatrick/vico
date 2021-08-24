package pl.patrykgoworowski.liftchart_common.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.axis.AxisManager
import pl.patrykgoworowski.liftchart_common.axis.model.DataSetModel
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel
import pl.patrykgoworowski.liftchart_common.data_set.renderer.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.dimensions.DataSetInsetter
import pl.patrykgoworowski.liftchart_common.dimensions.Dimensions
import pl.patrykgoworowski.liftchart_common.dimensions.MutableDimensions
import pl.patrykgoworowski.liftchart_common.dimensions.floatDimensions
import kotlin.math.max

open class VirtualLayout(
    var isLTR: Boolean
) {

    private val tempInsetters = ArrayList<DataSetInsetter>(5)
    private val finalInsets: MutableDimensions = floatDimensions()
    private val tempInsets: MutableDimensions = floatDimensions()

    open fun <Model : EntriesModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetRenderer<Model>,
        model: Model,
        dataSetModel: DataSetModel,
        axisManager: AxisManager,
        vararg dataSetInsetter: DataSetInsetter?,
    ) {
        tempInsetters.clear()
        finalInsets.clear()
        tempInsets.clear()
        axisManager.isLTR = isLTR
        axisManager.addInsetters(tempInsetters)
        dataSetInsetter.forEach { it?.let(tempInsetters::add) }

        tempInsetters.forEach { insetter ->
            insetter.getVerticalInsets(tempInsets, model, dataSetModel)
            finalInsets.setAllGreater(tempInsets)
        }

        val availableHeight = contentBounds.height() - finalInsets.vertical

        tempInsetters.forEach { insetter ->
            insetter.getHorizontalInsets(tempInsets, availableHeight, model, dataSetModel)
            finalInsets.setAllGreater(tempInsets)
        }

        dataSet.setBounds(
            left = contentBounds.left + finalInsets.getLeft(isLTR),
            top = contentBounds.top + finalInsets.top,
            right = contentBounds.right - finalInsets.getRight(isLTR),
            bottom = contentBounds.bottom - finalInsets.bottom
        )
        axisManager.setAxesBounds(contentBounds, dataSet.bounds, finalInsets)
    }

    private fun MutableDimensions.setAllGreater(other: Dimensions) {
        start = max(start, other.start)
        top = max(top, other.top)
        end = max(end, other.end)
        bottom = max(bottom, other.bottom)
    }

}