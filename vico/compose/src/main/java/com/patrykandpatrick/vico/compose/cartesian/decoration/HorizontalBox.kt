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

package com.patrykandpatrick.vico.compose.cartesian.decoration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalBox
import com.patrykandpatrick.vico.core.common.HorizontalPosition
import com.patrykandpatrick.vico.core.common.VerticalPosition
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore

/** Creates and remembers a [HorizontalBox]. */
@Composable
public fun rememberHorizontalBox(
  y: (ExtraStore) -> ClosedFloatingPointRange<Double>,
  box: ShapeComponent,
  labelComponent: TextComponent? = null,
  label: (ExtraStore) -> CharSequence = { HorizontalBox.getLabel(y(it)) },
  horizontalLabelPosition: HorizontalPosition = HorizontalPosition.Start,
  verticalLabelPosition: VerticalPosition = VerticalPosition.Top,
  labelRotationDegrees: Float = 0f,
  verticalAxisPosition: Axis.Position.Vertical? = null,
): HorizontalBox =
  remember(
    y,
    box,
    labelComponent,
    label,
    horizontalLabelPosition,
    verticalLabelPosition,
    labelRotationDegrees,
    verticalAxisPosition,
  ) {
    HorizontalBox(
      y,
      box,
      labelComponent,
      label,
      horizontalLabelPosition,
      verticalLabelPosition,
      labelRotationDegrees,
      verticalAxisPosition,
    )
  }
