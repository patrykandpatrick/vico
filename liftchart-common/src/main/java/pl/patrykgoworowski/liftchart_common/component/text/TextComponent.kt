package pl.patrykgoworowski.liftchart_common.component.text

import android.graphics.Canvas
import android.graphics.Color.DKGRAY
import android.graphics.Color.LTGRAY
import android.graphics.Paint
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import pl.patrykgoworowski.liftchart_common.DEF_LABEL_LINE_COUNT
import pl.patrykgoworowski.liftchart_common.component.ShapeComponent
import pl.patrykgoworowski.liftchart_common.component.dimension.DefaultMargins
import pl.patrykgoworowski.liftchart_common.component.dimension.DefaultPadding
import pl.patrykgoworowski.liftchart_common.component.dimension.Margins
import pl.patrykgoworowski.liftchart_common.component.dimension.Padding
import pl.patrykgoworowski.liftchart_common.extension.half
import pl.patrykgoworowski.liftchart_common.extension.lineHeight
import pl.patrykgoworowski.liftchart_common.extension.measureText
import pl.patrykgoworowski.liftchart_common.extension.sp
import pl.patrykgoworowski.liftchart_common.path.pillShape
import pl.patrykgoworowski.liftchart_common.text.staticLayout
import kotlin.math.roundToInt

public open class TextComponent(
    color: Int = DKGRAY,
    textSize: Float = 12f.sp,
    public val ellipsize: TextUtils.TruncateAt = TextUtils.TruncateAt.END,
    public val lineCount: Int = DEF_LABEL_LINE_COUNT,
    public open val background: ShapeComponent? = ShapeComponent(pillShape(), LTGRAY),
) : Padding by DefaultPadding(), Margins by DefaultMargins() {

    public val textPaint = TextPaint()

    public val lineHeight: Int
        get() = textPaint.lineHeight.roundToInt()

    public val allLinesHeight: Int
        get() = lineHeight * lineCount

    public var isLTR: Boolean = true
    public var color: Int by textPaint::color
    public var textSize: Float by textPaint::textSize
    public var textAlign: Paint.Align by textPaint::textAlign
    public var typeface: Typeface by textPaint::typeface
    public var rotationDegrees: Float = 0f
    private var layout: StaticLayout = staticLayout("", textPaint, 0)

    private val layoutCache = HashMap<Int, StaticLayout>()

    init {
        textPaint.color = color
        textPaint.textSize = textSize
    }

    public fun drawText(
        canvas: Canvas,
        text: CharSequence,
        textX: Float,
        textY: Float,
        horizontalPosition: HorizontalPosition = HorizontalPosition.Center,
        verticalPosition: VerticalPosition = VerticalPosition.Center,
        width: Int = Int.MAX_VALUE,
    ) {

        if (text.isBlank()) return
        val layoutWidth = minOf(textPaint.measureText(text).toInt(), width)
        layout = getLayout(text, layoutWidth)
        val layoutHeight = layout.height

        val textStartPosition = horizontalPosition.getTextStartPosition(textX, layoutWidth)
        val textTopPosition = verticalPosition.getTextTopPosition(textY, layoutHeight)

        background?.draw(
            canvas = canvas,
            left = textStartPosition - padding.getLeft(isLTR),
            top = textTopPosition - ((layoutHeight / 2) + padding.top),
            right = textStartPosition + layoutWidth + padding.getRight(isLTR),
            bottom = textTopPosition + ((layoutHeight / 2) + padding.bottom)
        )

        val centeredY = textTopPosition - layoutHeight.half

        canvas.save()
        if (rotationDegrees != 0f) {
            canvas.rotate(45f, textStartPosition, textTopPosition)
        }
        canvas.translate(textStartPosition, centeredY)

        layout.draw(canvas)

        canvas.restore()
    }

    private fun HorizontalPosition.getTextStartPosition(baseXPosition: Float, width: Int) = when (this) {
        HorizontalPosition.Start ->
            if (isLTR) getTextLeftPosition(baseXPosition)
            else getTextRightPosition(baseXPosition, width)
        HorizontalPosition.Center ->
            baseXPosition - width.half
        HorizontalPosition.End ->
            if (isLTR) getTextRightPosition(baseXPosition, width)
            else getTextLeftPosition(baseXPosition)
    }

    private fun getTextLeftPosition(baseXPosition: Float): Float =
        baseXPosition + padding.getLeft(isLTR) + margins.getLeft(isLTR)

    private fun getTextRightPosition(baseXPosition: Float, width: Int): Float =
        baseXPosition - (padding.getRight(isLTR) + margins.getRight(isLTR) + width)

    private fun VerticalPosition.getTextTopPosition(
        textY: Float,
        layoutHeight: Int,
    ) = when (this) {
        VerticalPosition.Top -> textY + layoutHeight.half
        VerticalPosition.Center -> textY
        VerticalPosition.Bottom -> textY - layoutHeight.half
    }

    public fun getWidth(text: CharSequence): Float {
        return textPaint.measureText(text) + padding.horizontal + margins.horizontal
    }

    public fun getHeight(
        text: CharSequence = TEXT_MEASUREMENT_CHAR,
        width: Int = Int.MAX_VALUE,
    ): Float {
        return getLayout(text, width).height + padding.vertical + margins.vertical
    }

    public fun clearLayoutCache() {
        layoutCache.clear()
    }

    private fun getLayout(text: CharSequence, width: Int): StaticLayout =
        layoutCache.getOrPut(text.hashCode() + 31 * width) {
            staticLayout(text, textPaint, width, maxLines = lineCount, ellipsize = ellipsize)
        }

    companion object {
        const val TEXT_MEASUREMENT_CHAR = "1"
    }

}