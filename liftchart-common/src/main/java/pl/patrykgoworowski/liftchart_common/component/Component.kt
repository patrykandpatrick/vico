/*
 * Copyright (c) 2021. Patryk Goworowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.patrykgoworowski.liftchart_common.component

import android.graphics.Canvas
import android.graphics.RectF
import pl.patrykgoworowski.liftchart_common.component.dimension.DefaultMargins
import pl.patrykgoworowski.liftchart_common.component.dimension.Margins

public abstract class Component() : Margins by DefaultMargins() {

    abstract fun draw(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    )

    open fun draw(
        canvas: Canvas,
        bounds: RectF
    ) {
        draw(canvas, bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

}