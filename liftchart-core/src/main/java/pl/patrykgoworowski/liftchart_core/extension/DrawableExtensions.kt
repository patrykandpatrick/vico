package pl.patrykgoworowski.liftchart_core.extension

import android.graphics.drawable.Drawable

fun Drawable.setBounds(left: Float, top: Float, right: Float, bottom: Float) {
    setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
}