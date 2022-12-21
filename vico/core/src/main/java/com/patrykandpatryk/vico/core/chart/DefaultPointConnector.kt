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

package com.patrykandpatryk.vico.core.chart

import android.graphics.Path
import android.graphics.RectF
import com.patrykandpatryk.vico.core.DefaultDimens
import com.patrykandpatryk.vico.core.chart.line.LineChart
import com.patrykandpatryk.vico.core.chart.segment.SegmentProperties
import com.patrykandpatryk.vico.core.component.shape.extension.horizontalCubicTo
import kotlin.math.abs

/**
 * The default implementation of [LineChart.LineSpec.PointConnector]. This uses cubic bezier curves.
 *
 * @property cubicStrength the strength of the cubic bezier curve between each point on the line.
 *
 * @see LineChart.LineSpec.PointConnector
 */
public class DefaultPointConnector(
    private val cubicStrength: Float = DefaultDimens.CUBIC_STRENGTH,
) : LineChart.LineSpec.PointConnector {

    public override fun connect(
        path: Path,
        prevX: Float,
        prevY: Float,
        x: Float,
        y: Float,
        segmentProperties: SegmentProperties,
        bounds: RectF,
    ) {
        path.horizontalCubicTo(
            prevX = prevX,
            prevY = prevY,
            x = x,
            y = y,
            curvature = segmentProperties.marginWidth * cubicStrength *
                (abs(x = y - prevY) / bounds.bottom * CUBIC_Y_MULTIPLIER).coerceAtMost(maximumValue = 1f),
        )
    }

    private companion object {

        const val CUBIC_Y_MULTIPLIER = 4
    }
}
