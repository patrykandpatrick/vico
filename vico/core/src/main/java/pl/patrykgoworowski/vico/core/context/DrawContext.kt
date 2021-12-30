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

package pl.patrykgoworowski.vico.core.context

import android.graphics.Canvas

public interface DrawContext : MeasureContext {
    public val canvas: Canvas

    public fun saveCanvas(): Int = canvas.save()

    public fun withOtherCanvas(canvas: Canvas, block: (DrawContext) -> Unit)

    public fun clipRect(left: Float, top: Float, right: Float, bottom: Float) {
        canvas.clipRect(left, top, right, bottom)
    }

    public fun restoreCanvas() {
        canvas.restore()
    }

    public fun restoreCanvasToCount(count: Int) {
        canvas.restoreToCount(count)
    }
}
