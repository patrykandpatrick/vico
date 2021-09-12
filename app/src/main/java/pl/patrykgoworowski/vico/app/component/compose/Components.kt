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

package pl.patrykgoworowski.vico.app.component.compose

import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.patrykgoworowski.vico.compose.component.dashedShape
import pl.patrykgoworowski.vico.compose.component.dimension.setPadding
import pl.patrykgoworowski.vico.compose.component.markerComponent
import pl.patrykgoworowski.vico.compose.component.overlayingComponent
import pl.patrykgoworowski.vico.compose.component.rectComponent
import pl.patrykgoworowski.vico.compose.component.textComponent
import pl.patrykgoworowski.vico.compose.extension.pixels
import pl.patrykgoworowski.vico.compose.extension.setShadow
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.marker.Marker
import pl.patrykgoworowski.vico.core.path.Shapes.pillShape
import pl.patrykgoworowski.vico.core.path.corner.MarkerCorneredShape

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

    val indicatorInner = ShapeComponent(pillShape, colors.surface.toArgb())
    val indicatorCenter = ShapeComponent(pillShape, Color.White.toArgb())
    val indicatorOuter = ShapeComponent(pillShape, Color.White.toArgb())

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
            shape = pillShape,
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
            corneredShape = pillShape,
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
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .verticalScroll(scrollState, true),
        verticalArrangement = verticalArrangement,
    ) {
        content()
    }
}
