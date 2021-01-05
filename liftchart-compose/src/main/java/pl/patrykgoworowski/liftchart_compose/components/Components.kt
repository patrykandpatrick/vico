package pl.patrykgoworowski.liftchart_compose.components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.tooling.preview.Preview
import pl.patrykgoworowski.liftchart_core.data_set.AnyEntry
import pl.patrykgoworowski.liftchart_core.data_set.DataSet
import pl.patrykgoworowski.liftchart_core.data_set.bar.BarDataSet
import pl.patrykgoworowski.liftchart_core.entry.entriesOf

@Composable
fun ChartContent(modifier: Modifier = Modifier, dataSet: DataSet) {

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

@Preview
@Composable
fun PreviewChartContent() {
    val dataSet = BarDataSet<AnyEntry>()
    dataSet.setEntries(entriesOf(1 to 1, 2 to 2, 3 to 3, 5 to 8))
    ChartContent(dataSet = dataSet)
}