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

package pl.patrykgoworowski.vico.sample.component

import android.graphics.Color
import android.text.TextUtils
import androidx.annotation.ColorInt
import pl.patrykgoworowski.vico.core.component.OverlayingComponent
import pl.patrykgoworowski.vico.core.component.marker.MarkerComponent
import pl.patrykgoworowski.vico.core.component.shape.DashedShape
import pl.patrykgoworowski.vico.core.component.shape.LineComponent
import pl.patrykgoworowski.vico.core.component.shape.ShapeComponent
import pl.patrykgoworowski.vico.core.component.shape.Shapes.pillShape
import pl.patrykgoworowski.vico.core.component.shape.cornered.Corner
import pl.patrykgoworowski.vico.core.component.shape.cornered.MarkerCorneredShape
import pl.patrykgoworowski.vico.core.component.text.buildTextComponent
import pl.patrykgoworowski.vico.core.dimensions.MutableDimensions
import pl.patrykgoworowski.vico.core.extension.copyColor
import pl.patrykgoworowski.vico.core.marker.Marker

internal fun getMarker(
    @ColorInt labelColor: Int,
    @ColorInt bubbleColor: Int,
    @ColorInt indicatorInnerColor: Int,
    @ColorInt guidelineColor: Int,
): Marker {

    val label = buildTextComponent {
        color = labelColor
        ellipsize = TextUtils.TruncateAt.END
        lineCount = 1
        padding = MutableDimensions(startDp = 8f, topDp = 4f, endDp = 8f, bottomDp = 4f)
        background = ShapeComponent(
            shape = MarkerCorneredShape(all = Corner.FullyRounded),
            color = bubbleColor,
        ).setShadow(radius = 4f, dy = 2f, applyElevationOverlay = true)
    }

    val indicatorInner = ShapeComponent(
        shape = pillShape,
        color = indicatorInnerColor,
    )

    val indicatorCenter = ShapeComponent(
        shape = pillShape,
        color = Color.WHITE,
    )

    val indicatorOuter = ShapeComponent(
        shape = pillShape,
        color = Color.WHITE,
    )

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
        )
    )

    return MarkerComponent(
        label = label,
        indicator = indicator,
        guideline = guideline,
    ).apply {
        indicatorSizeDp = INDICATOR_SIZE_DP
        onApplyEntryColor = { entryColor ->
            indicatorOuter.color = entryColor.copyColor(alpha = 32)
            with(indicatorCenter) {
                color = entryColor
                setShadow(
                    radius = 12f,
                    color = entryColor,
                )
            }
        }
    }
}

private const val GUIDELINE_ALPHA = 0.2f
private const val INDICATOR_SIZE_DP = 36f
