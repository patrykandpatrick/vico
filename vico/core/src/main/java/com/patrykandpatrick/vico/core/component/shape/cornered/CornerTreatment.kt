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

package com.patrykandpatrick.vico.core.component.shape.cornered

import android.graphics.Path
import com.patrykandpatrick.vico.core.annotation.LongParameterListDrawFunction

/**
 * Defines a shape corner style.
 */
public interface CornerTreatment {

    /**
     * Draws a shape corner of the appropriate style.
     * @param x1 the horizontal coordinate of the starting point.
     * @param y1 the vertical coordinate of the starting point.
     * @param x2 the horizontal coordinate of the end point.
     * @param y2 the vertical coordinate of the end point.
     * @param cornerLocation the location of the corner.
     * @path the [Path] to use to draw the corner.
     */
    @LongParameterListDrawFunction
    public fun createCorner(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        cornerLocation: CornerLocation,
        path: Path,
    )
}
