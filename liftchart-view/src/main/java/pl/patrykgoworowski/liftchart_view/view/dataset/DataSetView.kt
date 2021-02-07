package pl.patrykgoworowski.liftchart_view.view.dataset

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.ViewDataSetRenderer
import pl.patrykgoworowski.liftchart_view.extension.*

class DataSetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bounds = RectF()
    private val updateRequestListener: UpdateRequestListener = object : UpdateRequestListener {

        private var previousDataSetWidth = 0

        override fun invoke() {
            val dataSet = dataSet ?: return
            if (ViewCompat.isAttachedToWindow(this@DataSetView)) {
                val dataSetWidth = dataSet.getMeasuredWidth()
                val parentOrOwnWidth = parentOrOwnWidth
                if (widthIsWrapContent &&
                    width != dataSetWidth &&
                    dataSetWidth <= parentOrOwnWidth &&
                    previousDataSetWidth <= parentOrOwnWidth
                ) {
                    requestLayout()
                } else {
                    dataSet.setBounds(bounds)
                    invalidate()
                }
            }
        }
    }

    var dataSet: ViewDataSetRenderer? = null
        set(value) {
            field?.removeListener(updateRequestListener)
            field = value
            value?.addListener(updateRequestListener)
            value?.setBounds(bounds)
        }

    override fun onDraw(canvas: Canvas) {
        dataSet?.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> dataSet.getMeasuredWidth() + horizontalPadding
            MeasureSpec.AT_MOST -> minOf(
                dataSet.getMeasuredWidth() + horizontalPadding,
                widthMeasureSpec.specSize
            )
            else -> measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)
        }
        val height = measureDimension(MeasureSpec.getSize(heightMeasureSpec), heightMeasureSpec)
        setMeasuredDimension(width, height)

        bounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
        dataSet?.setBounds(bounds)
    }

    private fun ViewDataSetRenderer?.getMeasuredWidth() = this?.getMeasuredWidth() ?: 0

}