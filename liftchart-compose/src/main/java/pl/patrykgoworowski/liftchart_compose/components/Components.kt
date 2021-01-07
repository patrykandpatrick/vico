package pl.patrykgoworowski.liftchart_compose.components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import pl.patrykgoworowski.liftchart_common.data_set.DataSetRenderer

@Composable
fun ChartContent(modifier: Modifier = Modifier, dataSet: DataSetRenderer) {

    //dataSet.setColor(Color.Magenta)
    val bounds = RectF()

    Canvas(modifier) {
        bounds.set(0f, 0f, size.width, size.height)
        dataSet.setBounds(bounds)

        this.drawIntoCanvas { canvas ->
            dataSet.draw(canvas.nativeCanvas, 0f)
        }
    }
}
