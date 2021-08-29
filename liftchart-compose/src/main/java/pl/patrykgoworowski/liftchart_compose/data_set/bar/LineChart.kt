package pl.patrykgoworowski.liftchart_compose.data_set.bar

import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.patrykgoworowski.liftchart_common.component.Component
import pl.patrykgoworowski.liftchart_common.component.shape.shader.DynamicShader
import pl.patrykgoworowski.liftchart_common.data_set.line.LineDataSet
import pl.patrykgoworowski.liftchart_compose.extension.pixels

@Composable
fun lineDataSet(
    point: Component? = null,
    pointSize: Dp = 6.dp,
    spacing: Dp = 16.dp,
    lineWidth: Dp = 2.dp,
    lineColor: Color = Color.LightGray,
    lineBackgroundShader: DynamicShader? = null,
    lineStrokeCap: StrokeCap = StrokeCap.Round,
    cubicStrength: Float = 1f,
    minX: Float? = null,
    maxX: Float? = null,
    minY: Float? = null,
    maxY: Float? = null,
): LineDataSet {
    val dataSet = remember { LineDataSet() }
    return dataSet.apply {
        this.point = point
        this.pointSize = pointSize.pixels
        this.spacing = spacing.pixels
        this.lineWidth = lineWidth.pixels
        this.lineColor = lineColor.toArgb()
        this.lineBackgroundShader = lineBackgroundShader
        this.lineStrokeCap = lineStrokeCap.paintCap
        this.cubicStrength = cubicStrength
        this.minX = minX
        this.maxX = maxX
        this.minY = minY
        this.maxY = maxY
    }
}

private val StrokeCap.paintCap: Paint.Cap
    get() = when (this) {
        StrokeCap.Butt -> Paint.Cap.BUTT
        StrokeCap.Round -> Paint.Cap.ROUND
        StrokeCap.Square -> Paint.Cap.SQUARE
        else -> throw IllegalArgumentException()
    }