/*
 * Copyright 2022 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.sample.util

import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import androidx.annotation.ColorInt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.core.chart.insets.Insets
import com.patrykandpatrick.vico.core.chart.segment.SegmentProperties
import com.patrykandpatrick.vico.core.component.OverlayingComponent
import com.patrykandpatrick.vico.core.component.marker.MarkerComponent
import com.patrykandpatrick.vico.core.component.shape.DashedShape
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.component.shape.ShapeComponent
import com.patrykandpatrick.vico.core.component.shape.Shapes.pillShape
import com.patrykandpatrick.vico.core.component.shape.cornered.Corner
import com.patrykandpatrick.vico.core.component.shape.cornered.MarkerCorneredShape
import com.patrykandpatrick.vico.core.component.text.textComponent
import com.patrykandpatrick.vico.core.context.MeasureContext
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions
import com.patrykandpatrick.vico.core.extension.copyColor
import com.patrykandpatrick.vico.core.marker.Marker

internal fun getMarker(
    @ColorInt labelColor: Int,
    @ColorInt bubbleColor: Int,
    @ColorInt indicatorInnerColor: Int,
    @ColorInt guidelineColor: Int,
): Marker {

    val labelBackgroundShape = MarkerCorneredShape(all = Corner.FullyRounded)
    val label = textComponent {
        color = labelColor
        ellipsize = TextUtils.TruncateAt.END
        lineCount = 1
        padding = MutableDimensions(horizontalDp = 8f, verticalDp = 4f)
        typeface = Typeface.MONOSPACE
        background = ShapeComponent(shape = labelBackgroundShape, color = bubbleColor)
            .setShadow(radius = SHADOW_RADIUS, dy = SHADOW_DY, applyElevationOverlay = true)
    }

    val indicatorInner = ShapeComponent(shape = pillShape, color = indicatorInnerColor)
    val indicatorCenter = ShapeComponent(shape = pillShape, color = Color.WHITE)
    val indicatorOuter = ShapeComponent(shape = pillShape, color = Color.WHITE)

    val indicator = OverlayingComponent(
        outer = indicatorOuter,
        innerPaddingAllDp = 10f,
        inner = OverlayingComponent(
            outer = indicatorCenter,
            inner = indicatorInner,
            innerPaddingAllDp = 5f,
        ),
    )

    val guideline = LineComponent(
        color = guidelineColor.copyColor(alpha = GUIDELINE_ALPHA),
        thicknessDp = 2f,
        shape = DashedShape(
            shape = pillShape,
            dashLengthDp = 8f,
            gapLengthDp = 4f,
        ),
    )

    return object : MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
    ) {
        init {
            indicatorSizeDp = INDICATOR_SIZE_DP
            onApplyEntryColor = { entryColor ->
                indicatorOuter.color = entryColor.copyColor(alpha = 32)
                with(indicatorCenter) {
                    color = entryColor
                    setShadow(radius = 12f, color = entryColor)
                }
            }
        }

        override fun getInsets(
            context: MeasureContext,
            outInsets: Insets,
            segmentProperties: SegmentProperties,
        ) = with(context) {
            outInsets.top = label.getHeight(context) + labelBackgroundShape.tickSizeDp.pixels +
                SHADOW_RADIUS.pixels * SHADOW_RADIUS_TO_PX_MULTIPLIER - SHADOW_DY.pixels
        }
    }
}

@Composable
internal fun marker(): Marker = with(MaterialTheme.colorScheme) {
    getMarker(
        labelColor = onSurface.toArgb(),
        bubbleColor = surface.toArgb(),
        indicatorInnerColor = surface.toArgb(),
        guidelineColor = onSurface.toArgb(),
    )
}

private const val SHADOW_RADIUS = 4f
private const val SHADOW_RADIUS_TO_PX_MULTIPLIER = 1.3f
private const val SHADOW_DY = 2f
private const val GUIDELINE_ALPHA = 0.2f
private const val INDICATOR_SIZE_DP = 36f
