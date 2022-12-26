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

package com.patrykandpatrick.vico.core.component

import com.patrykandpatrick.vico.core.context.DrawContext
import com.patrykandpatrick.vico.core.debug.DebugHelper

/**
 * A [Component] composed out of two [Component]s, with one drawn over the other.
 * @property outer the outer (background) [Component].
 * @property inner the inner (foreground) [Component].
 * @property innerPaddingStartDp the start padding between the inner and outer components.
 * @property innerPaddingTopDp the top padding between the inner and outer components.
 * @property innerPaddingEndDp the end padding between the inner and outer components.
 * @property innerPaddingBottomDp the bottom padding between the inner and outer components.
 */
public class OverlayingComponent(
    public val outer: Component,
    public val inner: Component,
    public val innerPaddingStartDp: Float = 0f,
    public val innerPaddingTopDp: Float = 0f,
    public val innerPaddingEndDp: Float = 0f,
    public val innerPaddingBottomDp: Float = 0f,
) : Component() {

    public constructor(
        outer: Component,
        inner: Component,
        innerPaddingAllDp: Float = 0f,
    ) : this(
        outer = outer,
        inner = inner,
        innerPaddingStartDp = innerPaddingAllDp,
        innerPaddingTopDp = innerPaddingAllDp,
        innerPaddingEndDp = innerPaddingAllDp,
        innerPaddingBottomDp = innerPaddingAllDp,
    )

    init {
        inner.margins.set(
            startDp = innerPaddingStartDp,
            topDp = innerPaddingTopDp,
            endDp = innerPaddingEndDp,
            bottomDp = innerPaddingBottomDp,
        )
    }

    override fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
    ): Unit = with(context) {
        val leftWithMargin = left + margins.startDp.pixels
        val topWithMargin = top + margins.topDp.pixels
        val rightWithMargin = right - margins.endDp.pixels
        val bottomWithMargin = bottom - margins.bottomDp.pixels

        outer.draw(context, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
        inner.draw(context, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)

        DebugHelper.drawDebugBounds(
            context = context,
            left = left,
            top = top,
            right = right,
            bottom = bottom,
        )
    }
}
