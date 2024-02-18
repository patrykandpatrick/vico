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

package com.patrykandpatrick.vico.core.common

import android.graphics.RectF

/**
 * [MeasureContext] holds data used by various chart components during the measuring and drawing phases.
 */
public interface MeasureContext : PreMeasureContext {
    /**
     * The bounds of the canvas that will be used to draw the chart and its components.
     */
    public val canvasBounds: RectF
}
