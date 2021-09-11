package pl.patrykgoworowski.liftchart_common.extension

import android.content.res.Resources

inline val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

inline val Int.dp: Float
    get() = (this * Resources.getSystem().displayMetrics.density)

inline val Float.sp: Float
    get() = this * Resources.getSystem().displayMetrics.scaledDensity

inline val Int.sp: Int
    get() = (this * Resources.getSystem().displayMetrics.scaledDensity).toInt()

inline val Float.pxToDp: Float
    get() = this / Resources.getSystem().displayMetrics.density

inline val Float.dpInt: Int
    get() = dp.toInt()
