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

package pl.patrykgoworowski.vico.core.component

import android.graphics.Canvas

class OverlayingComponent(
    public val outer: Component,
    public val inner: Component,
    innerPaddingStart: Float = 0f,
    innerPaddingTop: Float = 0f,
    innerPaddingEnd: Float = 0f,
    innerPaddingBottom: Float = 0f,
) : Component() {

    constructor(
        outer: Component,
        inner: Component,
        innerPaddingAll: Float = 0f,
    ) : this(outer, inner, innerPaddingAll, innerPaddingAll, innerPaddingAll, innerPaddingAll)

    init {
        inner.margins.set(
            innerPaddingStart,
            innerPaddingTop,
            innerPaddingEnd,
            innerPaddingBottom
        )
    }

    override fun draw(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        val leftWithMargin = left + margins.start
        val topWithMargin = top + margins.top
        val rightWithMargin = right - margins.end
        val bottomWithMargin = bottom - margins.bottom

        outer.draw(canvas, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
        inner.draw(canvas, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
    }
}
