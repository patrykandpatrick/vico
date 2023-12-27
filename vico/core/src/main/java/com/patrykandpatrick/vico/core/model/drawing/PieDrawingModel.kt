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

package com.patrykandpatrick.vico.core.model.drawing

import com.patrykandpatrick.vico.core.chart.pie.PieChart
import com.patrykandpatrick.vico.core.extension.lerp
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.math.interpolateOpacity
import kotlin.math.max

/**
 * Houses drawing information for a [PieChart]. [opacity] is the slices’ opacity.
 */
public class PieDrawingModel(slices: List<SliceInfo>, public val opacity: Float = 1f) :
    DrawingModel<PieDrawingModel.SliceInfo>(slices.toTransformationMap()) {
    override fun transform(
        drawingInfo: List<Map<Float, SliceInfo>>,
        from: DrawingModel<SliceInfo>?,
        fraction: Float,
    ): DrawingModel<SliceInfo> {
        val oldOpacity = (from as PieDrawingModel?)?.opacity.orZero
        val oldSlices = from?.firstOrNull()?.values?.toList() ?: emptyList()
        val slices = drawingInfo.firstOrNull()?.values?.toList() ?: emptyList()
        val sliceCount = max(oldSlices.size, slices.size)
        val combinedSlices =
            List(sliceCount) { index ->
                slices.getOrNull(index) ?: oldSlices[index]
            }
        return PieDrawingModel(combinedSlices, oldOpacity.lerp(opacity, fraction))
    }

    /**
     * Houses positional information for a [PieChart]’s slice.
     */
    public class SliceInfo(
        public val degrees: Float,
        public val label: CharSequence?,
        public val sliceOpacity: Float = 1f,
        public val labelOpacity: Float = 1f,
    ) : DrawingInfo {
        override fun transform(
            from: DrawingInfo?,
            fraction: Float,
        ): DrawingInfo {
            val oldSliceInfo = from as? SliceInfo
            val oldDegrees = oldSliceInfo?.degrees.orZero
            val oldLabel = oldSliceInfo?.label
            val labelChanged = oldLabel != null && oldLabel != label
            val sliceOpacity =
                when {
                    oldSliceInfo == null || oldSliceInfo.degrees == 0f -> fraction
                    degrees == 0f -> 1f - fraction
                    else -> 1f
                }
            return SliceInfo(
                degrees = oldDegrees.lerp(degrees, fraction),
                label = if (fraction < 0.5f) oldLabel else label,
                sliceOpacity = sliceOpacity,
                labelOpacity = if (labelChanged) interpolateOpacity(fraction) else 1f,
            )
        }
    }

    private companion object {
        private fun List<SliceInfo>.toTransformationMap(): List<Map<Float, SliceInfo>> {
            var index = 0
            return listOf(associateBy { (index++).toFloat() })
        }
    }
}
