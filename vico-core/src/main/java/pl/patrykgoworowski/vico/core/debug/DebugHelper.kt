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

package pl.patrykgoworowski.vico.core.debug

import android.graphics.Color
import android.graphics.Paint
import pl.patrykgoworowski.vico.core.draw.DrawContext

internal object DebugHelper {
    public var enabled: Boolean = false

    public var strokeWidthDp: Float = 1f
    public var debugPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.MAGENTA
    }

    public fun drawDebugBounds(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ) = with(context) {
        if (!enabled) return@with
        debugPaint.strokeWidth = strokeWidthDp.pixels
        canvas.drawRect(left, top, right, bottom, debugPaint)
    }
}
