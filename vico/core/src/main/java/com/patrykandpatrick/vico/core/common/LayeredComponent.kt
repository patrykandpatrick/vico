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
 * Draws two [Component]s on top of each other.
 *
 * @property back the back [Component].
 * @property front the front [Component].
 * @property padding the padding between [back] and [front].
 * @property margins the margins around the [LayeredComponent].
 */
public open class LayeredComponent(
  protected val back: Component,
  protected val front: Component,
  protected val padding: Insets = Insets.Zero,
  protected val margins: Insets = Insets.Zero,
) : Component {
  override fun draw(
    context: DrawingContext,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
  ): Unit =
    with(context) {
      val leftWithMargin = left + margins.getLeft(context)
      val topWithMargin = top + margins.topDp.pixels
      val rightWithMargin = right - margins.getRight(context)
      val bottomWithMargin = bottom - margins.bottomDp.pixels

      back.draw(context, leftWithMargin, topWithMargin, rightWithMargin, bottomWithMargin)
      front.draw(
        context,
        leftWithMargin + padding.getLeft(context),
        topWithMargin + padding.topDp.pixels,
        rightWithMargin - padding.getRight(context),
        bottomWithMargin - padding.bottomDp.pixels,
      )
    }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is LayeredComponent &&
        back == other.back &&
        front == other.front &&
        padding == other.padding &&
        margins == other.margins

  override fun hashCode(): Int {
    var result = back.hashCode()
    result = 31 * result + front.hashCode()
    result = 31 * result + padding.hashCode()
    result = 31 * result + margins.hashCode()
    return result
  }
}
