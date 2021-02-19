package pl.patrykgoworowski.liftchart_common.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.BoundsAware
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position.*
import pl.patrykgoworowski.liftchart_common.data_set.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

public open class VirtualLayout(
    var isLTR: Boolean
) {

    public open fun getMeasuredWidth(
        dataSetWidth: Int,
        entriesModel: EntriesModel,
        axisMap: Map<Position, AxisRenderer>,
    ): Int =
        dataSetWidth + axisMap.values.sumBy { axis ->
            if (axis is VerticalAxis) axis.getSize(entriesModel).toInt() else 0
        }

    public open fun setBounds(
        contentBounds: RectF,
        dataSet: BoundsAware,
        entriesModel: EntriesModel,
        axisMap: Map<Position, AxisRenderer>,
    ) {

        val startSize = axisMap[START]?.getSize(entriesModel) ?: 0f
        val topSize = axisMap[TOP]?.getSize(entriesModel) ?: 0f
        val endSize = axisMap[END]?.getSize(entriesModel) ?: 0f
        val bottomSize = axisMap[BOTTOM]?.getSize(entriesModel) ?: 0f

        dataSet.setBounds(
            left = contentBounds.left + startSize,
            top = contentBounds.top + topSize,
            right = contentBounds.right - endSize,
            bottom = contentBounds.bottom - bottomSize)

        axisMap.forEach { (position, axisRenderer) ->
            axisRenderer.setDataSetBounds(dataSet.bounds)
            when (position) {
                START -> axisRenderer.setBounds(
                    left = if (isLTR) contentBounds.left else contentBounds.right - endSize,
                    top = contentBounds.top + topSize,
                    right = if (isLTR) contentBounds.left + startSize else contentBounds.right,
                    bottom = contentBounds.bottom - bottomSize
                )
                TOP -> axisRenderer.setBounds(
                    left = contentBounds.left + startSize,
                    top = contentBounds.top,
                    right = contentBounds.right - endSize,
                    bottom = contentBounds.top + topSize
                )
                END -> axisRenderer.setBounds(
                    left = if (isLTR) contentBounds.right - endSize else contentBounds.left,
                    top = contentBounds.top + topSize,
                    right = if (isLTR) contentBounds.right else contentBounds.left + endSize,
                    bottom = contentBounds.bottom - bottomSize
                )
                BOTTOM -> axisRenderer.setBounds(
                    left = contentBounds.left + startSize,
                    top = contentBounds.bottom - bottomSize,
                    right = contentBounds.right - endSize,
                    bottom = contentBounds.bottom
                )
            }
        }
    }

}