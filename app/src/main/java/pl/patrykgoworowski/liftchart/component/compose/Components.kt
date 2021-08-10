package pl.patrykgoworowski.liftchart.component.compose

import android.text.TextUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.extension.copyColor
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape
import pl.patrykgoworowski.liftchart_common.path.pillShape
import pl.patrykgoworowski.liftchart_compose.component.*
import pl.patrykgoworowski.liftchart_compose.component.dimension.setPadding
import pl.patrykgoworowski.liftchart_compose.extension.pixels
import pl.patrykgoworowski.liftchart_compose.extension.setShadow

@Composable
fun markerComponent(): Marker {
    val colors = MaterialTheme.colors
    val label = textComponent(
            color = colors.onSurface,
            textSize = 12f.sp,
            ellipsize = TextUtils.TruncateAt.END,
            lineCount = 1,
            background = null
    ).apply {
        setPadding(8f.dp, 4f.dp)
    }

    val indicatorInner = ShapeComponent(pillShape(), colors.surface.toArgb())
    val indicatorCenter = ShapeComponent(pillShape(), Color.White.toArgb())
    val indicatorOuter = ShapeComponent(pillShape(), Color.White.toArgb())

    val indicator = overlayingComponent(
            outer = indicatorOuter,
            inner = overlayingComponent(
                    outer = indicatorCenter,
                    inner = indicatorInner,
                    innerPaddingAll = 5.dp
            ),
            innerPaddingAll = 10.dp,
    )

    val guideline = rectComponent(
            color = colors.onSurface.copy(alpha = 0.18f),
            thickness = 2f.dp,
            shape = dashedShape(
                    shape = pillShape(),
                    dashLength = 8f.dp,
                    gapLength = 4f.dp
            )
    )

    val indicatorShadowSize = 12.dp.pixels

    return markerComponent(
            label = label,
            indicator = indicator,
            guideline = guideline,
            shape = MarkerCorneredShape(
                    corneredShape = pillShape(),
                    tickSize = 6.dp.pixels
            ),
            markerBackgroundColor = colors.surface,
    ).apply {
        onApplyEntryColor = { color ->
            indicatorCenter.color = color
            indicatorCenter.setShadow(indicatorShadowSize, color = color)
            indicatorOuter.color = color.copyColor(alpha = 32)
        }
        indicatorSize = 36.dp.pixels
        setShadow(4f.dp, dy = 2f.dp)
    }

}

@Composable
fun ScrollableColumn(
        modifier: Modifier = Modifier,
        content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
            modifier = modifier
                    .verticalScroll(scrollState, true)
    ) {
        content()
    }
}
