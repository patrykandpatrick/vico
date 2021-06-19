package pl.patrykgoworowski.liftchart_common.text

import android.graphics.Rect
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import pl.patrykgoworowski.liftchart_common.extension.getFieldValue
import pl.patrykgoworowski.liftchart_common.extension.setFieldValue

private const val MAX_LINES_FIELD = "mMaximumVisibleLineCount"
private const val LINE_COUNT_FIELD = "mLineCount"

@Suppress("DEPRECATION")
fun staticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    maxLines: Int = Int.MAX_VALUE,
    startIndex: Int = 0,
    endIndex: Int = source.length,
    spacingMultiplication: Float = 1f,
    spacingAddition: Float = 0f,
    includePadding: Boolean = true,
    ellipsize: TextUtils.TruncateAt? = null,
    ellipsizedWidth: Int = width,
    align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
    ): StaticLayout =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout
            .Builder
            .obtain(
                source,
                startIndex,
                endIndex,
                paint,
                width
            ).setAlignment(align)
            .setLineSpacing(spacingAddition, spacingMultiplication)
            .setIncludePad(includePadding)
            .setEllipsize(ellipsize)
            .setEllipsizedWidth(ellipsizedWidth)
            .setMaxLines(maxLines)
            .build()
    } else {
        StaticLayout(
            source,
            startIndex,
            endIndex,
            paint,
            width,
            align,
            spacingMultiplication,
            spacingAddition,
            includePadding,
            ellipsize,
            ellipsizedWidth,
        ).setLineCount(maxLines)
    }

var StaticLayout.maxLines: Int
    set(value) = setFieldValue(MAX_LINES_FIELD, value)
    get() = getFieldValue(MAX_LINES_FIELD)

fun StaticLayout.setLineCount(count: Int): StaticLayout {
    setFieldValue(LINE_COUNT_FIELD, count)
    return this
}

fun StaticLayout.getBounds(outBounds: Rect) {
    getLineBounds(0, outBounds)
    if (lineCount > 1) {
        val top = outBounds.top
        getLineBounds(lineCount - 1, outBounds)
        outBounds.top = top
    }
}

val StaticLayout.widestLineWidth: Float
get() =
    (0 until lineCount).maxOf { lineIndex ->
        getLineWidth(lineIndex)
    }
