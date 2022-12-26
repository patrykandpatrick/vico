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

package com.patrykandpatrick.vico.core.component.dimension

import com.patrykandpatrick.vico.core.dimensions.Dimensions
import com.patrykandpatrick.vico.core.dimensions.MutableDimensions

/**
 * Allows a component to implement margins.
 */
public interface Margins {

    /**
     * The current margins.
     */
    public val margins: MutableDimensions

    /**
     * Updates each margin individually.
     */
    public fun setMargins(
        start: Float = 0f,
        top: Float = 0f,
        end: Float = 0f,
        bottom: Float = 0f,
    ) {
        margins.set(start, top, end, bottom)
    }

    /**
     * Sets a common size for each margin.
     */
    public fun setMargins(
        all: Float = 0f,
    ) {
        margins.set(all)
    }
}

/**
 * Sets the values of these [Margins] to those of the provided [Dimensions].
 *
 * @param margins the [Dimensions] whose values to assign to these [Margins].
 */
public fun Margins.setMargins(margins: Dimensions) {
    this.margins.set(margins)
}
