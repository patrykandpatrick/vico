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

package com.patrykandpatrick.vico.core.chart.column

import com.patrykandpatrick.vico.core.entry.diff.DrawingModel
import com.patrykandpatrick.vico.core.extension.orZero

/**
 * Houses drawing information for a [ColumnChart]. [opacity] is the columns’ opacity.
 */
public class ColumnChartDrawingModel(entries: List<Map<Float, ColumnInfo>>, public val opacity: Float = 1f) :
    DrawingModel<ColumnChartDrawingModel.ColumnInfo>(entries) {

    override fun transform(
        drawingInfo: List<Map<Float, ColumnInfo>>,
        from: DrawingModel<ColumnInfo>?,
        fraction: Float,
    ): DrawingModel<ColumnInfo> {
        val oldOpacity = (from as ColumnChartDrawingModel?)?.opacity.orZero
        return ColumnChartDrawingModel(drawingInfo, oldOpacity + (opacity - oldOpacity) * fraction)
    }

    /**
     * Houses positional information for a [ColumnChart]’s column. [height] expresses the column’s height as a fraction
     * of the [ColumnChart]’s height.
     */
    public class ColumnInfo(public val height: Float) : DrawingInfo {
        override fun transform(from: DrawingInfo?, fraction: Float): DrawingInfo {
            val oldHeight = (from as? ColumnInfo)?.height.orZero
            return ColumnInfo(oldHeight + (height - oldHeight) * fraction)
        }
    }
}
