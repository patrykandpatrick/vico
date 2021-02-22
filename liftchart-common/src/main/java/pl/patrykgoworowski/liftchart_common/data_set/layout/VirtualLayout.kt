package pl.patrykgoworowski.liftchart_common.data_set.layout

import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.AxisRenderer
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position
import pl.patrykgoworowski.liftchart_common.data_set.axis.Position.*
import pl.patrykgoworowski.liftchart_common.data_set.axis.VerticalAxis
import pl.patrykgoworowski.liftchart_common.data_set.entry.collection.EntriesModel

public open class VirtualLayout(
    var isLTR: Boolean
) {

    public open fun <Model> getMeasuredWidth(
        dataSet: DataSetRenderer<Model>,
        model: Model,
        axisMap: Map<Position, AxisRenderer>,
    ): Int =
        dataSet.getMeasuredWidth(model) + axisMap.values.sumBy { axis ->
            if (axis is VerticalAxis) axis.getSize(dataSet.getAxisModel(model)).toInt() else 0
        }

    public open fun <Model: EntriesModel> setBounds(
        contentBounds: RectF,
        dataSet: DataSetRenderer<Model>,
        model: Model,
        axisMap: Map<Position, AxisRenderer>,
    ) {
        val startSize = axisMap[START]?.getSize(model) ?: 0f
        val topSize = axisMap[TOP]?.getSize(model) ?: 0f
        val endSize = axisMap[END]?.getSize(model) ?: 0f
        val bottomSize = axisMap[BOTTOM]?.getSize(model) ?: 0f

        val startExtend = axisMap[START]?.drawExtend ?: 0f
        val topExtend = axisMap[TOP]?.drawExtend ?: 0f
        val endExtend = axisMap[END]?.drawExtend ?: 0f
        val bottomExtend = axisMap[BOTTOM]?.drawExtend ?: 0f

        dataSet.setBounds(
            left = contentBounds.left + startSize,
            top = contentBounds.top + topSize,
            right = contentBounds.right - endSize,
            bottom = contentBounds.bottom - bottomSize
        )

        axisMap.forEach { (position, axisRenderer) ->
            axisRenderer.dataSetBounds.set(dataSet.bounds)
            when (position) {
                START -> {
                    axisRenderer.setBounds(
                        left = if (isLTR) contentBounds.left else contentBounds.right - endSize,
                        top = contentBounds.top + topSize,
                        right = if (isLTR) contentBounds.left + startSize else contentBounds.right,
                        bottom = contentBounds.bottom - bottomSize
                    )
                    axisRenderer.extendBounds.set(axisRenderer.bounds)
//                    axisRenderer.extendBounds.updateBy(top = -topExtend, bottom = bottomExtend)
                }
                TOP -> {
                    axisRenderer.setBounds(
                        left = contentBounds.left + startSize,
                        top = contentBounds.top,
                        right = contentBounds.right - endSize,
                        bottom = contentBounds.top + topSize// + axisRenderer.drawExtend
                    )
                    axisRenderer.extendBounds.set(axisRenderer.bounds)
//                    axisRenderer.extendBounds.updateBy(left = -startExtend, right = endExtend)
                }
                END -> {
                    axisRenderer.setBounds(
                        left = if (isLTR) contentBounds.right - endSize else contentBounds.left,
                        top = contentBounds.top + topSize,
                        right = if (isLTR) contentBounds.right else contentBounds.left + endSize,
                        bottom = contentBounds.bottom - bottomSize
                    )
                    axisRenderer.extendBounds.set(axisRenderer.bounds)
                    //axisRenderer.extendBounds.updateBy(top = -topExtend, bottom = bottomExtend)
                }

                BOTTOM -> {
                    axisRenderer.setBounds(
                        left = contentBounds.left + startSize,
                        top = contentBounds.bottom - bottomSize,
                        right = contentBounds.right - endSize,
                        bottom = contentBounds.bottom
                    )
                    axisRenderer.extendBounds.set(axisRenderer.bounds)
//                    axisRenderer.extendBounds.updateBy(left = -startExtend, right = endExtend)
                }
            }
        }
    }

}