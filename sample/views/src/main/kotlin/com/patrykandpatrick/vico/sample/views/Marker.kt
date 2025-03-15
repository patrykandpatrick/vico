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

package com.patrykandpatrick.vico.sample.views

import android.content.Context
import android.text.Layout
import androidx.core.content.ContextCompat
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
import com.patrykandpatrick.vico.core.common.LayeredComponent
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.copyColor
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.DashedShape
import com.patrykandpatrick.vico.core.common.shape.MarkerCorneredShape

internal fun getMarker(
  context: Context,
  valueFormatter: DefaultCartesianMarker.ValueFormatter =
    DefaultCartesianMarker.ValueFormatter.default(),
  showIndicator: Boolean = true,
): CartesianMarker {
  val labelBackgroundShape = MarkerCorneredShape(CorneredShape.Corner.Rounded)
  val labelBackground =
    ShapeComponent(
      fill = Fill(ContextCompat.getColor(context, R.color.background)),
      shape = labelBackgroundShape,
      strokeThicknessDp = 1f,
      strokeFill = Fill(ContextCompat.getColor(context, R.color.outline)),
    )
  val label =
    TextComponent(
      color = ContextCompat.getColor(context, R.color.on_surface),
      textAlignment = Layout.Alignment.ALIGN_CENTER,
      padding = Insets(horizontalDp = 8f, verticalDp = 4f),
      background = labelBackground,
      minWidth = TextComponent.MinWidth.fixed(valueDp = 40f),
    )
  val indicatorFrontComponent =
    ShapeComponent(Fill(ContextCompat.getColor(context, R.color.surface)), CorneredShape.Pill)
  val guideline =
    LineComponent(
      fill = Fill(ContextCompat.getColor(context, R.color.outline)),
      thicknessDp = 1f,
      shape = DashedShape(),
    )
  return DefaultCartesianMarker(
    label = label,
    valueFormatter = valueFormatter,
    indicator =
      if (showIndicator) {
        { color ->
          LayeredComponent(
            back = ShapeComponent(Fill(color.copyColor(alpha = 0.15f)), CorneredShape.Pill),
            front =
              LayeredComponent(
                back = ShapeComponent(Fill(color), CorneredShape.Pill),
                front = indicatorFrontComponent,
                padding = Insets(allDp = 5f),
              ),
            padding = Insets(allDp = 10f),
          )
        }
      } else {
        null
      },
    indicatorSizeDp = 36f,
    guideline = guideline,
  )
}
