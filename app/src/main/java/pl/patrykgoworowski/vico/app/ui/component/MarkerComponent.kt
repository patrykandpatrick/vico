/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.vico.app.ui.component

import android.text.TextUtils
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.vico.compose.dimensions.dimensionsOf
import pl.patrykgoworowski.vico.compose.component.lineComponent
import pl.patrykgoworowski.vico.compose.component.marker.markerComponent
import pl.patrykgoworowski.vico.compose.component.overlayingComponent
import pl.patrykgoworowski.vico.compose.component.shape.dashedShape
import pl.patrykgoworowski.vico.compose.component.shape.markerCorneredShape
import pl.patrykgoworowski.vico.compose.component.shape.textComponent
import pl.patrykgoworowski.vico.compose.component.shapeComponent
import pl.patrykgoworowski.vico.compose.extension.indicatorSize
import pl.patrykgoworowski.vico.compose.extension.setShadow
import pl.patrykgoworowski.vico.compose.style.currentChartStyle
import pl.patrykgoworowski.vico.core.component.shape.Shapes
import pl.patrykgoworowski.vico.core.component.shape.Shapes.pillShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.Corner
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.marker.Marker

@Composable
internal fun markerComponent(): Marker {
    val colors = MaterialTheme.colors
    val label = textComponent(
        color = colors.onSurface,
        textSize = 12f.sp,
        ellipsize = TextUtils.TruncateAt.END,
        lineCount = 1,
        background = shapeComponent(
            shape = Shapes.markerCorneredShape(all = Corner.FullyRounded),
            color = colors.surface,
        ).setShadow(radius = 4.dp, dy = 2.dp, applyElevationOverlay = true),
        padding = dimensionsOf(
            horizontal = currentChartStyle.marker.horizontalPadding,
            vertical = currentChartStyle.marker.verticalPadding,
        )
    )

    val indicatorInner = shapeComponent(pillShape, colors.surface)
    val indicatorCenter = shapeComponent(pillShape, Color.White)
    val indicatorOuter = shapeComponent(pillShape, Color.White)

    val indicator = overlayingComponent(
        outer = indicatorOuter,
        inner = overlayingComponent(
            outer = indicatorCenter,
            inner = indicatorInner,
            innerPaddingAll = 5.dp
        ),
        innerPaddingAll = 10.dp,
    )

    val guideline = lineComponent(
        color = colors.onSurface.copy(alpha = 0.18f),
        thickness = 2f.dp,
        shape = Shapes.dashedShape(
            shape = pillShape,
            dashLength = 8f.dp,
            gapLength = 4f.dp
        )
    )
    return markerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
    ).apply {
        onApplyEntryColor = { color ->
            indicatorCenter.color = color
            indicatorCenter.setShadow(radius = 12.dp, color = Color(color))
            indicatorOuter.color = color.copyColor(alpha = 32)
        }
        indicatorSize = currentChartStyle.marker.indicatorSize
    }
}
