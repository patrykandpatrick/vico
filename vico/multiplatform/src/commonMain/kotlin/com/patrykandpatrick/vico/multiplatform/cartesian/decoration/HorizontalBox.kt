/*
 * Copyright 2025 by Patryk Goworowski and Patrick Michalik.
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

package com.patrykandpatrick.vico.multiplatform.cartesian.decoration

import com.patrykandpatrick.vico.multiplatform.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.Axis
import com.patrykandpatrick.vico.multiplatform.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.multiplatform.common.Position
import com.patrykandpatrick.vico.multiplatform.common.component.ShapeComponent
import com.patrykandpatrick.vico.multiplatform.common.component.TextComponent
import com.patrykandpatrick.vico.multiplatform.common.data.ExtraStore
import com.patrykandpatrick.vico.multiplatform.common.format
import com.patrykandpatrick.vico.multiplatform.common.getEnd
import com.patrykandpatrick.vico.multiplatform.common.getStart
import com.patrykandpatrick.vico.multiplatform.common.half
import com.patrykandpatrick.vico.multiplatform.common.inBounds
import com.patrykandpatrick.vico.multiplatform.common.unaryMinus

/**
 * A [Decoration] that highlights a _y_ range.
 *
 * @property y returns the _y_ range.
 * @property box the box [ShapeComponent].
 * @property labelComponent the label [TextComponent].
 * @property label returns the label text.
 * @property horizontalLabelPosition defines the horizontal position of the label.
 * @property verticalLabelPosition defines the vertical position of the label.
 * @property labelRotationDegrees the rotation of the label (in degrees).
 * @property verticalAxisPosition the position of the [VerticalAxis] whose scale the [HorizontalBox]
 *   should use when interpreting [y].
 */
public class HorizontalBox(
  private val y: (ExtraStore) -> ClosedFloatingPointRange<Double>,
  private val box: ShapeComponent,
  private val labelComponent: TextComponent? = null,
  private val label: (ExtraStore) -> CharSequence = { getLabel(y(it)) },
  private val horizontalLabelPosition: Position.Horizontal = Position.Horizontal.Start,
  private val verticalLabelPosition: Position.Vertical = Position.Vertical.Top,
  private val labelRotationDegrees: Float = 0f,
  private val verticalAxisPosition: Axis.Position.Vertical? = null,
) : Decoration {
  override fun drawOverLayers(context: CartesianDrawingContext) {
    with(context) {
      val yRange = ranges.getYRange(verticalAxisPosition)
      val y = y(model.extraStore)
      val label = label(model.extraStore)
      val topY =
        layerBounds.bottom -
          ((y.endInclusive - yRange.minY) / yRange.length).toFloat() * layerBounds.height
      val bottomY =
        (layerBounds.bottom - (y.start - yRange.minY) / yRange.length * layerBounds.height)
          .toFloat()
      val labelY =
        when (verticalLabelPosition) {
          Position.Vertical.Top -> topY
          Position.Vertical.Center -> (topY + bottomY).half
          Position.Vertical.Bottom -> bottomY
        }
      box.draw(context, layerBounds.left, topY, layerBounds.right, bottomY)
      labelComponent?.draw(
        context = context,
        text = label,
        x =
          when (horizontalLabelPosition) {
            Position.Horizontal.Start -> layerBounds.getStart(isLtr)
            Position.Horizontal.Center -> layerBounds.center.x
            Position.Horizontal.End -> layerBounds.getEnd(isLtr)
          },
        y = labelY,
        horizontalPosition = -horizontalLabelPosition,
        verticalPosition =
          verticalLabelPosition.inBounds(
            bounds = layerBounds,
            componentHeight =
              labelComponent.getHeight(
                context = context,
                text = label,
                rotationDegrees = labelRotationDegrees,
              ),
            referenceY = labelY,
          ),
        maxWidth = layerBounds.width.toInt(),
        rotationDegrees = labelRotationDegrees,
      )
    }
  }

  override fun equals(other: Any?): Boolean =
    this === other ||
      other is HorizontalBox &&
        box == other.box &&
        labelComponent == other.labelComponent &&
        horizontalLabelPosition == other.horizontalLabelPosition &&
        verticalLabelPosition == other.verticalLabelPosition &&
        labelRotationDegrees == other.labelRotationDegrees &&
        verticalAxisPosition == other.verticalAxisPosition

  override fun hashCode(): Int {
    var result = y.hashCode()
    result = 31 * result + box.hashCode()
    result = 31 * result + labelComponent.hashCode()
    result = 31 * result + label.hashCode()
    result = 31 * result + horizontalLabelPosition.hashCode()
    result = 31 * result + verticalLabelPosition.hashCode()
    result = 31 * result + labelRotationDegrees.hashCode()
    result = 31 * result + verticalAxisPosition.hashCode()
    return result
  }

  internal companion object {
    fun getLabel(y: ClosedFloatingPointRange<Double>): String =
      "${y.start.format()}–${y.endInclusive.format()}"
  }
}
