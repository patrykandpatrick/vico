package pl.patrykgoworowski.liftchart_common.extension

import android.graphics.Paint

private val fm: Paint.FontMetrics = Paint.FontMetrics()

val Paint.lineHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.bottom - fm.top + fm.leading
    }

val Paint.textHeight: Float
    get() {
        getFontMetrics(fm)
        return fm.descent - fm.ascent
    }

fun Paint.measureText(text: CharSequence) =
    measureText(text, 0, text.length)