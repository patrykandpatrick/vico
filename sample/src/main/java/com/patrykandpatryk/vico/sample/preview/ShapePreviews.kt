/*
 * Copyright 2022 Patryk Goworowski and Patryk Michalik
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

package com.patrykandpatryk.vico.sample.preview

import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.patrykandpatryk.vico.R
import com.patrykandpatryk.vico.compose.component.shape.dashedShape
import com.patrykandpatryk.vico.core.component.shape.Shape
import com.patrykandpatryk.vico.core.component.shape.Shapes
import com.patrykandpatryk.vico.core.draw.drawContext

@Composable
private fun PreviewShape(shape: Shape) {
    val paint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF212121.toInt() } }
    val path = remember { Path() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.LightGray, shape = RoundedCornerShape(size = 4.dp))
            .padding(8.dp),
    ) {
        shape.drawShape(
            context = drawContext(drawContext.canvas.nativeCanvas),
            paint = paint,
            path = path,
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
        )
    }
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun PreviewRectShape() {
    PreviewShape(shape = Shapes.rectShape)
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun PreviewPillShape() {
    PreviewShape(shape = Shapes.pillShape)
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun RoundedCorner25Shape() {
    PreviewShape(shape = Shapes.roundedCornerShape(allPercent = 25))
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun RoundedCornerCustom1Shape() {
    PreviewShape(shape = Shapes.roundedCornerShape(topLeftPercent = 50, bottomRightPercent = 75))
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun CutCorner25Shape() {
    PreviewShape(shape = Shapes.cutCornerShape(allPercent = 25))
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun CutCornerCustom1Shape() {
    PreviewShape(shape = Shapes.cutCornerShape(topRightPercent = 100, bottomLeftPercent = 15))
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun DrawableShape() {
    PreviewShape(
        shape = Shapes.drawableShape(
            drawable = getDrawable(id = R.drawable.ic_baseline_android_24),
            keepAspectRatio = true,
            otherShape = Shapes.pillShape,
        ),
    )
}

@Composable
@Preview(widthDp = 50, heightDp = 100)
public fun DrawableShape2() {
    PreviewShape(
        shape = Shapes.drawableShape(
            drawable = getDrawable(id = R.drawable.ic_baseline_android_24),
            keepAspectRatio = true,
            otherShape = Shapes.pillShape,
        ),
    )
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun DrawableShapeStretched() {
    PreviewShape(shape = Shapes.drawableShape(getDrawable(id = R.drawable.ic_baseline_android_24)))
}

@Composable
@Preview(widthDp = 100, heightDp = 50)
public fun DashedCutCornerCustomShape() {
    PreviewShape(
        shape = Shapes.dashedShape(
            shape = Shapes.cutCornerShape(topRightPercent = 50, bottomLeftPercent = 50),
            dashLength = 24.dp,
            gapLength = 8.dp,
        ),
    )
}

@Composable
private fun getDrawable(@DrawableRes id: Int): Drawable =
    ResourcesCompat.getDrawable(LocalContext.current.resources, id, null)!!
