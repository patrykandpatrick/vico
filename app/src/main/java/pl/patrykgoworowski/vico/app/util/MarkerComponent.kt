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

package pl.patrykgoworowski.vico.app.util

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import pl.patrykgoworowski.vico.R
import pl.patrykgoworowski.vico.app.extension.getThemeColor
import pl.patrykgoworowski.vico.core.Dimens
import pl.patrykgoworowski.vico.core.component.MarkerComponent
import pl.patrykgoworowski.vico.core.component.OverlayingComponent
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.text.TextComponent
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.Shapes.pillShape
import pl.patrykgoworowski.vico.core.component.shape.corner.MarkerCorneredShape

fun getMarkerComponent(context: Context): Marker {

    val label = TextComponent(
        color = context.getThemeColor(R.attr.colorOnSurface),
        textSizeSp = 12f,
        ellipsize = TextUtils.TruncateAt.END,
        lineCount = 1,
        background = null
    ).apply {
        setPadding(8f, 4f)
    }

    val colorSurface = context.getThemeColor(R.attr.colorSurface)
    val indicatorInner = ShapeComponent(pillShape, colorSurface)
    val indicatorCenter = ShapeComponent(pillShape, Color.WHITE)
    val indicatorOuter = ShapeComponent(pillShape, Color.WHITE)

    val indicator = OverlayingComponent(
        outer = indicatorOuter,
        inner = OverlayingComponent(
            outer = indicatorCenter,
            inner = indicatorInner,
            innerPaddingAllDp = 5f,
        ),
        innerPaddingAllDp = 10f,
    )

    val guideline = LineComponent(
        color = context.getThemeColor(R.attr.colorOnSurface).copyColor(alpha = 48),
        thicknessDp = 2f,
        shape = DashedShape(
            shape = pillShape,
            dashLengthDp = 8f,
            gapLengthDp = 4f,
        )
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
        shape = MarkerCorneredShape(
            corneredShape = pillShape,
            tickSize = 6f,
        ),
        markerBackgroundColor = colorSurface,
    ).apply {
        onApplyEntryColor = { color ->
            indicatorCenter.color = color
            indicatorCenter.setShadow(12f, color = color)
            indicatorOuter.color = color.copyColor(alpha = 32)
        }
        indicatorSize = Dimens.MARKER_INDICATOR_SIZE
        setShadow(4f, dy = 2f)
    }
}
