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

package com.patrykandpatrick.vico.core.chart.dimensions

import androidx.annotation.RestrictTo

/**
 * An implementation of [HorizontalDimensions] whose every property is mutable.
 */
public data class MutableHorizontalDimensions(
    override var xSpacing: Float = 0f,
    override var scalableStartPadding: Float = 0f,
    override var scalableEndPadding: Float = 0f,
    override var unscalableStartPadding: Float = 0f,
    override var unscalableEndPadding: Float = 0f,
) : HorizontalDimensions {
    /**
     * Updates the stored values.
     */
    public fun set(
        xSpacing: Float,
        scalableStartPadding: Float,
        scalableEndPadding: Float,
        unscalableStartPadding: Float,
        unscalableEndPadding: Float,
    ): MutableHorizontalDimensions =
        apply {
            this.xSpacing = xSpacing
            this.scalableStartPadding = scalableStartPadding
            this.scalableEndPadding = scalableEndPadding
            this.unscalableStartPadding = unscalableStartPadding
            this.unscalableEndPadding = unscalableEndPadding
        }

    /**
     * Ensures that the stored values are no smaller than the provided ones.
     */
    public fun ensureValuesAtLeast(
        xSpacing: Float = 0f,
        scalableStartPadding: Float = 0f,
        scalableEndPadding: Float = 0f,
        unscalableStartPadding: Float = 0f,
        unscalableEndPadding: Float = 0f,
    ): MutableHorizontalDimensions =
        set(
            this.xSpacing.coerceAtLeast(xSpacing),
            this.scalableStartPadding.coerceAtLeast(scalableStartPadding),
            this.scalableEndPadding.coerceAtLeast(scalableEndPadding),
            this.unscalableStartPadding.coerceAtLeast(unscalableStartPadding),
            this.unscalableEndPadding.coerceAtLeast(unscalableEndPadding),
        )

    /**
     * Clears the stored values.
     */
    public fun clear() {
        xSpacing = 0f
        scalableStartPadding = 0f
        scalableEndPadding = 0f
        unscalableStartPadding = 0f
        unscalableEndPadding = 0f
    }
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun MutableHorizontalDimensions.scale(factor: Float): MutableHorizontalDimensions =
    set(
        factor * xSpacing,
        factor * scalableStartPadding,
        factor * scalableEndPadding,
        unscalableStartPadding,
        unscalableEndPadding,
    )
