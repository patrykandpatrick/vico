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

package com.patrykandpatrick.vico.sample.previews

import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.patrykandpatrick.vico.R
import com.patrykandpatrick.vico.compose.common.shape.dashed
import com.patrykandpatrick.vico.compose.common.shape.toComposeShape
import com.patrykandpatrick.vico.core.common.drawContext
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import com.patrykandpatrick.vico.core.common.shape.Shape

@Composable
private fun PreviewShape(shape: Shape) {
  val black = 0xFF212121.toInt()
  val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { color = black } }
  val path = remember { Path() }

  Column(
    modifier =
      Modifier.width(100.dp)
        .background(color = Color.White, shape = RoundedCornerShape(size = 4.dp))
        .padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(text = "Canvas")

    Canvas(modifier = Modifier.fillMaxWidth().height(50.dp)) {
      shape.draw(
        context = drawContext(drawContext.canvas.nativeCanvas),
        paint = paint,
        path = path,
        left = 0f,
        top = 0f,
        right = size.width,
        bottom = size.height,
      )
    }

    if (shape is CorneredShape) {
      Spacer(modifier = Modifier.height(8.dp))

      Text(text = "Compose Shape")

      Box(
        modifier =
          Modifier.height(50.dp)
            .fillMaxWidth()
            .background(color = Color(black), shape = shape.toComposeShape())
      )
    }
  }
}

@Composable
@Preview
public fun PreviewRectShape() {
  PreviewShape(shape = Shape.Rectangle)
}

@Composable
@Preview
public fun PreviewPillShape() {
  PreviewShape(shape = Shape.Pill)
}

@Composable
@Preview
public fun RoundedCorner25Shape() {
  PreviewShape(shape = Shape.rounded(allPercent = 25))
}

@Composable
@Preview
public fun RoundedCornerCustom1Shape() {
  PreviewShape(shape = Shape.rounded(topLeftPercent = 50, bottomRightPercent = 75))
}

@Composable
@Preview
public fun CutCorner25Shape() {
  PreviewShape(shape = Shape.cut(allPercent = 25))
}

@Composable
@Preview
public fun CutCornerCustom1Shape() {
  PreviewShape(shape = Shape.cut(topRightPercent = 100, bottomLeftPercent = 15))
}

@Composable
@Preview
public fun DrawableShape() {
  PreviewShape(
    shape =
      Shape.drawable(
        drawable = getDrawable(id = R.drawable.ic_baseline_android_24),
        keepAspectRatio = true,
        otherShape = Shape.Pill,
      )
  )
}

@Composable
@Preview
public fun DrawableShape2() {
  PreviewShape(
    shape =
      Shape.drawable(
        drawable = getDrawable(id = R.drawable.ic_baseline_android_24),
        keepAspectRatio = true,
        otherShape = Shape.Pill,
      )
  )
}

@Composable
@Preview
public fun DrawableShapeStretched() {
  PreviewShape(shape = Shape.drawable(getDrawable(id = R.drawable.ic_baseline_android_24)))
}

@Composable
@Preview
public fun DashedCutCornerCustomShape() {
  PreviewShape(
    shape =
      Shape.dashed(
        shape = Shape.cut(topRightPercent = 50, bottomLeftPercent = 50),
        dashLength = 24.dp,
        gapLength = 8.dp,
      )
  )
}

@Composable
private fun getDrawable(@DrawableRes id: Int): Drawable =
  ResourcesCompat.getDrawable(LocalContext.current.resources, id, null)!!
