/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import pl.patrykgoworowski.liftchart_common.path.PillShape
import pl.patrykgoworowski.liftchart_common.path.corner.MarkerCorneredShape

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
    val indicatorInner = ShapeComponent(PillShape, colorSurface)
    val indicatorCenter = ShapeComponent(PillShape, Color.WHITE)
    val indicatorOuter = ShapeComponent(PillShape, Color.WHITE)

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
            shape = PillShape,
            dashLength = 8f.dp,
            gapLength = 4f.dp
        )
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
        shape = MarkerCorneredShape(
            corneredShape = PillShape,
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