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

package com.patrykandpatrick.vico.core.draw

import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.RestrictTo
import com.patrykandpatrick.vico.core.DefaultColors
import com.patrykandpatrick.vico.core.chart.layout.HorizontalLayout
import com.patrykandpatrick.vico.core.chart.values.ChartValues
import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.model.MutableExtraStore

/**
 * Calls the specified function block with [DrawContext.canvas] as its receiver.
 */
@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(message = "`withCanvas` is meant only for internal use.", level = DeprecationLevel.ERROR)
public inline fun DrawContext.withCanvas(block: Canvas.() -> Unit) {
    canvas.block()
}

/** @suppress */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public fun drawContext(
    canvas: Canvas,
    density: Float = 1f,
    isLtr: Boolean = true,
    elevationOverlayColor: Long = DefaultColors.Light.elevationOverlayColor,
    spToPx: (Float) -> Float = { it },
): DrawContext =
    object : DrawContext {
        override val canvasBounds: RectF = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
        override val elevationOverlayColor: Long = elevationOverlayColor
        override var canvas: Canvas = canvas
        override val density: Float = density
        override val isLtr: Boolean = isLtr
        override val scrollEnabled: Boolean = false
        override val chartValues: ChartValues = ChartValues.Empty
        override val horizontalLayout: HorizontalLayout = HorizontalLayout.Segmented
        override val extraStore: MutableExtraStore = MutableExtraStore()

        override fun withOtherCanvas(
            canvas: Canvas,
            block: (DrawContext) -> Unit,
        ) {
            val originalCanvas = this.canvas
            this.canvas = canvas
            block(this)
            this.canvas = originalCanvas
        }

        override fun spToPx(sp: Float): Float = spToPx(sp)
    }
