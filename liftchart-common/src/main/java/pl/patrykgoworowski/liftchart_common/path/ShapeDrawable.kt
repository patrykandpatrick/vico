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