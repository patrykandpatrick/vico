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

import com.patrykandpatrick.vico.core.common.component.Component

/**
 * Draws two [Component]s, [rear] and [front], on top of each other. [padding] defines the padding between them.
 */
public open class LayeredComponent(
    public val rear: Component,
    public val front: Component,
    public val padding: Dimensions,
) : Component {
    override val margins: MutableDimensions = MutableDimensions.empty()

    init {
        front.margins.set(padding)
    }

    override fun draw(
        context: DrawContext,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        opacity: Float,
    ): Unit =
        with(context) {
            val leftWithMargin = left + margins.startDp.pixels
            val topWithMargin = top + margins.topDp.pixels
            val rightWithMargin = right - margins.endDp.pixels
            val bottomWithMargin = bottom - margins.bottomDp.pixels

            rear.draw(context, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin, opacity)
            front.draw(context, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin, opacity)
        }
}
