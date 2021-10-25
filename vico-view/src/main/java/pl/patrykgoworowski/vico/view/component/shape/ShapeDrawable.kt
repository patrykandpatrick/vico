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

package pl.patrykgoworowski.vico.view.component.shape

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LayoutDirection
import pl.patrykgoworowski.vico.core.component.shape.Shape
import pl.patrykgoworowski.vico.core.draw.drawContext
import pl.patrykgoworowski.vico.view.extension.density
import pl.patrykgoworowski.vico.view.extension.fontScale
import pl.patrykgoworowski.vico.view.extension.isLtr

public class ShapeDrawable(
    private val shape: Shape,
    private val isLtr: Boolean,
    private val density: Float,
    private val fontScale: Float,
    private val width: Int = 0,
    private val height: Int = 0,
) : Drawable() {

    constructor(
        context: Context,
        shape: Shape,
        width: Int = 0,
        height: Int = 0,
    ) : this(
        shape = shape,
        density = context.density,
        fontScale = context.fontScale,
        isLtr = context.isLtr,
        width = width,
        height = height,
    )

    private val path: Path = Path()

    private var tintList: ColorStateList? = null

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEF_COLOR
    }

    init {
        setBounds(0, 0, width, height)
    }

    override fun draw(canvas: Canvas) {
        shape.drawShape(
            drawContext(
                canvas = canvas,
                density = density,
                fontScale = fontScale,
                isLtr = isLtr(),
            ),
            paint = paint,
            path = path,
            left = bounds.left.toFloat(),
            top = bounds.top.toFloat(),
            right = bounds.right.toFloat(),
            bottom = bounds.bottom.toFloat(),
        )
        path.reset()
    }

    private fun isLtr(): Boolean = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        layoutDirection == LayoutDirection.LTR
    } else {
        isLtr
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun setTintList(tint: ColorStateList?) {
        tintList = tint
        updateColor()
    }

    override fun setState(stateSet: IntArray): Boolean {
        val result = super.setState(stateSet)
        updateColor()
        return result
    }

    private fun updateColor() {
        paint.color = tintList?.getColorForState(state, DEF_COLOR) ?: DEF_COLOR
    }

    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun getIntrinsicWidth(): Int = width

    override fun getIntrinsicHeight(): Int = height

    companion object {
        const val DEF_COLOR = Color.BLACK
    }
}

fun Shape.toDrawable(
    context: Context,
    intrinsicWidth: Int = 0,
    intrinsicHeight: Int = 0,
): Drawable = ShapeDrawable(context, this, intrinsicWidth, intrinsicHeight)
