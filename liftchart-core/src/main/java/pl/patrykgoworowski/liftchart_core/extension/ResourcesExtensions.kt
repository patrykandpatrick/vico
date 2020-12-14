package pl.patrykgoworowski.liftchart_core.extension

import android.content.res.Resources

inline val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

inline val Int.dp: Int
    get() = toFloat().dp.toInt()