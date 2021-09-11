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

package pl.patrykgoworowski.liftchart_common.path

import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.Drawable

public class ShapeDrawable(
    private val shape: Shape,
    private val width: Int = 0,
    private val height: Int = 0,
) : Drawable() {

    private val path: Path = Path()
    private val rectF: RectF = RectF()

    private var tintList: ColorStateList? = null

    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = DEF_COLOR
    }

    init {
        setBounds(0, 0, width, height)
    }

    override fun draw(canvas: Canvas) {
        rectF.set(bounds)
        shape.drawShape(canvas, paint, path, rectF)
        path.reset()
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
    intrinsicWidth: Int = 0,
    intrinsicHeight: Int = 0,
): Drawable = ShapeDrawable(this, intrinsicWidth, intrinsicHeight)