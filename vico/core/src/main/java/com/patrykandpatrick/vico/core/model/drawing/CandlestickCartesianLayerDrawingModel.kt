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

package com.patrykandpatrick.vico.core.model.drawing

import com.patrykandpatrick.vico.core.chart.layer.CandlestickCartesianLayer
import com.patrykandpatrick.vico.core.extension.lerp
import com.patrykandpatrick.vico.core.extension.orZero

/**
 * Houses drawing information for a [CandlestickCartesianLayer]. [opacity] is the columns’ opacity.
 */
public class CandlestickCartesianLayerDrawingModel(
    public val entries: Map<Float, CandleInfo>,
    public val zeroY: Float,
    public val opacity: Float = 1f,
) : DrawingModel<CandlestickCartesianLayerDrawingModel.CandleInfo>(listOf(entries)) {
    override fun transform(
        drawingInfo: List<Map<Float, CandleInfo>>,
        from: DrawingModel<CandleInfo>?,
        fraction: Float,
    ): DrawingModel<CandleInfo> {
        val oldOpacity = (from as CandlestickCartesianLayerDrawingModel?)?.opacity.orZero
        val oldZeroY = from?.zeroY ?: zeroY
        return CandlestickCartesianLayerDrawingModel(
            entries = drawingInfo.first(),
            zeroY = oldZeroY.lerp(zeroY, fraction),
            opacity = oldOpacity.lerp(opacity, fraction),
        )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            other is CandlestickCartesianLayerDrawingModel &&
            entries == other.entries &&
            zeroY == other.zeroY &&
            opacity == other.opacity

    override fun hashCode(): Int = entries.hashCode() * 31 + zeroY.hashCode() * 31 + opacity.hashCode() * 31

    /**
     * Houses positional information for a [CandlestickCartesianLayer]’s column. TODO
     */
    public class CandleInfo(
        public val low: Float,
        public val high: Float,
        public val open: Float,
        public val close: Float,
    ) : DrawingInfo {
        override fun transform(
            from: DrawingInfo?,
            fraction: Float,
        ): DrawingInfo {
            val old = from as? CandleInfo
            val oldLow = old?.low.orZero
            val oldHigh = old?.high.orZero
            val oldOpen = old?.open.orZero
            val oldClose = old?.close.orZero
            return CandleInfo(
                oldLow.lerp(low, fraction),
                oldHigh.lerp(high, fraction),
                oldOpen.lerp(open, fraction),
                oldClose.lerp(close, fraction),
            )
        }

        override fun equals(other: Any?): Boolean =
            this === other ||
                other is CandleInfo &&
                low == other.low &&
                high == other.high &&
                open == other.open &&
                close == other.close

        override fun hashCode(): Int =
            low.hashCode() * 31 + high.hashCode() * 31 + open.hashCode() * 31 + close.hashCode() * 31
    }
}
