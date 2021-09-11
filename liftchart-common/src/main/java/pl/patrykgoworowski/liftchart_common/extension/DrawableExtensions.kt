package pl.patrykgoworowski.liftchart_common.extension

import android.graphics.drawable.Drawable

fun Drawable.setBounds(left: Float, top: Float, right: Float, bottom: Float) {
    setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}
