/*
 * Copyright 2024 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.core.cartesian.model

import com.patrykandpatrick.vico.core.cartesian.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.common.DrawingModel
import com.patrykandpatrick.vico.core.common.extension.lerp
import com.patrykandpatrick.vico.core.common.extension.orZero

/**
 * Houses drawing information for a [CandlestickCartesianLayer]. [opacity] is the columns’ opacity.
 */
public class CandlestickCartesianLayerDrawingModel(
    public val entries: Map<Float, CandleInfo>,
    public val opacity: Float = 1f,
) : DrawingModel<CandlestickCartesianLayerDrawingModel.CandleInfo>(listOf(entries)) {
    override fun transform(
        drawingInfo: List<Map<Float, CandleInfo>>,
        from: DrawingModel<CandleInfo>?,
        fraction: Float,
    ): DrawingModel<CandleInfo> {
        val oldOpacity = (from as CandlestickCartesianLayerDrawingModel?)?.opacity.orZero
        return CandlestickCartesianLayerDrawingModel(
            entries = drawingInfo.first(),
            opacity = oldOpacity.lerp(opacity, fraction),
        )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is CandlestickCartesianLayerDrawingModel && entries == other.entries && opacity == other.opacity

    override fun hashCode(): Int = 31 * entries.hashCode() + opacity.hashCode()

    /**
     * Houses positional information for a [CandlestickCartesianLayer]’s column. TODO
     */
    public class CandleInfo(
        public val bodyBottomY: Float,
        public val bodyTopY: Float,
        public val bottomWickY: Float,
        public val topWickY: Float,
    ) : DrawingInfo {
        override fun transform(
            from: DrawingInfo?,
            fraction: Float,
        ): DrawingInfo {
            val old = from as? CandleInfo
            val oldBodyBottomY = old?.bodyBottomY.orZero
            val oldBodyTopY = old?.bodyTopY.orZero
            val oldBottomWickY = old?.bottomWickY.orZero
            val oldTopWickY = old?.topWickY.orZero
            return CandleInfo(
                oldBodyBottomY.lerp(bodyBottomY, fraction),
                oldBodyTopY.lerp(bodyTopY, fraction),
                oldBottomWickY.lerp(bottomWickY, fraction),
                oldTopWickY.lerp(topWickY, fraction),
            )
        }

        override fun equals(other: Any?): Boolean =
            this === other ||
                other is CandleInfo &&
                bodyBottomY == other.bodyBottomY &&
                bodyTopY == other.bodyTopY &&
                bottomWickY == other.bottomWickY &&
                topWickY == other.topWickY

        override fun hashCode(): Int {
            var result = bodyBottomY.hashCode()
            result = 31 * result + bodyTopY.hashCode()
            result = 31 * result + bottomWickY.hashCode()
            result = 31 * result + topWickY.hashCode()
            return result
        }
    }
}
