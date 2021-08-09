package pl.patrykgoworowski.liftchart.component.view

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import pl.patrykgoworowski.liftchart.R
import pl.patrykgoworowski.liftchart.extension.getThemeColor
import pl.patrykgoworowski.liftchart_common.component.MarkerComponent
import pl.patrykgoworowski.liftchart_common.component.OverlayingComponent
import pl.patrykgoworowski.liftchart_common.component.shape.LineComponent
import pl.patrykgoworowski.liftchart_common.component.shape.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.text.TextComponent
import pl.patrykgoworowski.liftchart_common.extension.copyColor
import pl.patrykgoworowski.liftchart_common.extension.dp
import pl.patrykgoworowski.liftchart_common.extension.sp
import pl.patrykgoworowski.liftchart_common.marker.Marker
import pl.patrykgoworowski.liftchart_common.path.DashedShape
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape
import pl.patrykgoworowski.liftchart_common.path.pillShape

fun getMarkerComponent(context: Context): Marker {

    val label = TextComponent(
        color = context.getThemeColor(R.attr.colorOnSurface),
        textSize = 12f.sp,
        ellipsize = TextUtils.TruncateAt.END,
        lineCount = 1,
        background = null
    ).apply {
        setPadding(8f.dp, 4f.dp)
    }

    val colorSurface = context.getThemeColor(R.attr.colorSurface)
    val indicatorInner = ShapeComponent(pillShape(), colorSurface)
    val indicatorCenter = ShapeComponent(pillShape(), Color.WHITE)
    val indicatorOuter = ShapeComponent(pillShape(), Color.WHITE)

    val indicator = OverlayingComponent(
        outer = indicatorOuter,
        inner = OverlayingComponent(
            outer = indicatorCenter,
            inner = indicatorInner,
            innerPaddingAll = 5.dp
        ),
        innerPaddingAll = 10.dp,
    )

    val guideline = LineComponent(
        color = context.getThemeColor(R.attr.colorOnSurface).copyColor(alpha = 48),
        thickness = 2f.dp,
        shape = DashedShape(
            shape = pillShape(),
            dashLength = 8f.dp,
            gapLength = 4f.dp
        )
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
        shape = MarkerCorneredShape(
            corneredShape = pillShape(),
            tickSize = 6.dp
        ),
        markerBackgroundColor = colorSurface,
    ).apply {
        onApplyEntryColor = { color ->
            indicatorCenter.color = color
            indicatorCenter.setShadow(12.dp, color = color)
            indicatorOuter.color = color.copyColor(alpha = 32)
        }
        indicatorSize = 36.dp
        setShadow(4f.dp, dy = 2f.dp)
    }
}