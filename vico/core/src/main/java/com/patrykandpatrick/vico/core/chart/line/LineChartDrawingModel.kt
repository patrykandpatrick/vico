/*
 * Copyright 2023 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.chart.line

import com.patrykandpatrick.vico.core.entry.diff.DrawingModel
import com.patrykandpatrick.vico.core.extension.lerp
import com.patrykandpatrick.vico.core.extension.orZero

/**
 * Houses drawing information for a [LineChart]. [zeroY] is the y-axis position of the zero value.
 * [opacity] is the lines’ opacity.
 */
public class LineChartDrawingModel(
    pointInfo: List<Map<Float, PointInfo>>,
    public val zeroY: Float,
    public val opacity: Float = 1f,
) :
    DrawingModel<LineChartDrawingModel.PointInfo>(pointInfo) {
    override fun transform(
        drawingInfo: List<Map<Float, PointInfo>>,
        from: DrawingModel<PointInfo>?,
        fraction: Float,
    ): DrawingModel<PointInfo> {
        val oldOpacity = (from as LineChartDrawingModel?)?.opacity.orZero
        val oldZeroY = from?.zeroY ?: zeroY
        return LineChartDrawingModel(drawingInfo, oldZeroY.lerp(zeroY, fraction), oldOpacity.lerp(opacity, fraction))
    }

    /**
     * Houses positional information for a [LineChart]’s point. [y] expresses the distance of the point from the bottom
     * of the [LineChart] as a fraction of the [LineChart]’s height.
     */
    public class PointInfo(public val y: Float) : DrawingInfo {
        override fun transform(
            from: DrawingInfo?,
            fraction: Float,
        ): DrawingInfo {
            val oldY = (from as? PointInfo)?.y.orZero
            return PointInfo(oldY.lerp(y, fraction))
        }
    }
}
