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

import pl.patrykgoworowski.vico.core.debug.DebugHelper
import pl.patrykgoworowski.vico.core.draw.DrawContext

public class OverlayingComponent(
    public val outer: Component,
    public val inner: Component,
    public val insidePaddingStartDp: Float = 0f,
    public val insidePaddingTopDp: Float = 0f,
    public val insidePaddingEndDp: Float = 0f,
    public val insidePaddingBottomDp: Float = 0f,
) : Component() {

    public constructor(
        outer: Component,
        inner: Component,
        innerPaddingAllDp: Float = 0f,
    ) : this(
        outer = outer,
        inner = inner,
        insidePaddingStartDp = innerPaddingAllDp,
        insidePaddingTopDp = innerPaddingAllDp,
        insidePaddingEndDp = innerPaddingAllDp,
        insidePaddingBottomDp = innerPaddingAllDp
    )

    init {
        inner.margins.set(
            startDp = insidePaddingStartDp,
            topDp = insidePaddingTopDp,
            endDp = insidePaddingEndDp,
            bottomDp = insidePaddingBottomDp,
        )
    }

    override fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
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
            bottom = bottom
        )
    }
}
